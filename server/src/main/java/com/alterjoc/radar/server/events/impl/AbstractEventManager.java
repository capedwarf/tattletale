package com.alterjoc.radar.server.events.impl;

import java.io.IOException;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.server.dao.AuditLogDAO;
import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Event;
import com.alterjoc.radar.server.domain.Topic;
import com.alterjoc.radar.server.events.EventManager;
import com.alterjoc.radar.server.users.Limits;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.common.social.SocialEvent;
import org.jboss.capedwarf.server.api.domain.GeoPt;
import org.jboss.capedwarf.server.api.io.BlobService;
import org.jboss.capedwarf.server.api.quilifiers.Name;

/**
 * Abstract event manager.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractEventManager implements EventManager
{
   protected Logger log = Logger.getLogger(getClass().getName());

   private BlobService blobService;
   private EventDAO eventDAO;
   private TopicDAO topicDAO;
   private AuditLogDAO auditLogDAO;
   private Limits limits;
   private Cache cache;
   private javax.enterprise.event.Event<SocialEvent> socialEvent;

   public StatusInfo createEvent(HttpServletRequest req, HttpServletResponse resp, EventInfo eventInfo, Client client) throws IOException, ServletException
   {
      Topic topic = topicDAO.find(eventInfo.getTopicId());
      if (topic != null && topic.isActive())
      {
         if (limits.canCreateNewEvent(client.getId(), topic.getId()) == false)
            return new StatusInfo(Status.EXCEEDED_LIMIT);

         Event event = postEvent(eventInfo, topic, client);
         return new StatusInfo(Status.OK, event.getId());
      }
      else
      {
         return new StatusInfo(Status.NO_SUCH_ENTITY);
      }
   }

   public Event postEvent(EventInfo eventInfo, Topic topic, Client client)
   {
      Long clientId = client.getId();
      String username = client.getUsername();

      Event event = new Event();
      event.setTopicId(topic.getId());
      event.setTopic(topic);
      event.setTitle(eventInfo.getTitle());
      event.setComment(eventInfo.getComment());
      event.setAddress(eventInfo.getAddress());
      GeoPt point = new GeoPt(eventInfo.getLatitude() / (float) Constants.MIO, eventInfo.getLongitude() / (float) Constants.MIO);
      event.setPoint(point);
      byte[] bigImage = eventInfo.getPhoto();
      event.setBigImage(storeImage(bigImage));
      byte[] smallImage = eventInfo.getSmallPhoto();
      event.setSmallImage(storeImage(smallImage));
      event.setExpirationTime(eventInfo.getExpirationDelta());
      // client / publisher
      event.setPublisherId(clientId);
      event.setPublisherUsername(username);

      eventDAO.save(event); // should get event id here
      // could be a gap here -- but the chances are slim, at least I hope so :-)
      cache.remove(Constants.TOPIC_EVENTS + event.getTopicId());
      // audit log
      auditLogDAO.setLast(event);
      // social integration
      socialEvent.fire(event);

      eventInfo.setKind(topic.getKind());
      eventInfo.setEventId(event.getId()); // set the event id to eventInfo
      eventInfo.setClientId(clientId);

      return event;
   }

   /**
    * Store image.
    *
    * @param bytes the bytes
    * @return the blob key
    */
   protected String storeImage(byte[] bytes)
   {
      try
      {
         return blobService.storeBytes("image/gif", bytes);
      }
      catch (IOException e)
      {
         log.warning("Cannot store image: " + e);
         return null;
      }
   }

   @Inject
   public void setBlobService(BlobService blobService)
   {
      this.blobService = blobService;
   }

   @Inject
   public void setEventDAO(EventDAO eventDAO)
   {
      this.eventDAO = eventDAO;
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }

   @Inject
   public void setAuditLogDAO(AuditLogDAO auditLogDAO)
   {
      this.auditLogDAO = auditLogDAO;
   }

   @Inject
   public void setLimits(Limits limits)
   {
      this.limits = limits;
   }

   @Inject
   public void setCache(@Name("EventsCache") Cache cache)
   {
      this.cache = cache;
   }

   @Inject
   public void setSocialEvent(javax.enterprise.event.Event<SocialEvent> socialEvent)
   {
      this.socialEvent = socialEvent;
   }
}
