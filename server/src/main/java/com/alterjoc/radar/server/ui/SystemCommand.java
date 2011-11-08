package com.alterjoc.radar.server.ui;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.server.dao.ClientDAO;
import com.alterjoc.radar.server.dao.CommentDAO;
import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Comment;
import com.alterjoc.radar.server.domain.Event;
import com.alterjoc.radar.server.domain.Topic;
import com.alterjoc.radar.server.events.EventManager;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.server.api.security.Security;
import org.jboss.capedwarf.server.api.ui.Command;

/**
 * System command.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Named("sc")
@RequestScoped
public class SystemCommand extends Command implements EventProducer
{
   private ClientDAO clientDAO;
   private TopicDAO topicDAO;
   private EventDAO eventDAO;
   private CommentDAO commentDAO;
   private EventManager eventManager;

   /**
    * Get admin.
    *
    * @return the admin
    */
   private Client getAdmin()
   {
      Client admin = clientDAO.findClient(Constants.ADMINISTRATOR);
      if (admin == null)
         throw new IllegalArgumentException("No admin?");

      return admin;
   }

   /**
    * Send an administration event.
    *
    * @return next view
    */
   @Security
   public String submitEvent()
   {
      Topic topic = topicDAO.find(getLong("system", "topicId"));
      if (topic == null)
         throw new IllegalArgumentException("No such topic");

      String title = getParameter("system", "title");
      String comment = trim(getParameter("system", "comment"));
      String address = trim(getParameter("system", "address"));
      submitSystemEvent(topic, title, comment, address);

      return HOME;
   }

   @Security({"admin", "editor"})
   public void submitSystemEvent(Topic topic, String title, String comment, String address)
   {
      Client admin = getAdmin();

      EventInfo eventInfo = new EventInfo();
      eventInfo.setTitle(title);
      eventInfo.setComment(comment);
      eventInfo.setAddress(address);
      eventInfo.setExpirationDelta(org.jboss.capedwarf.common.Constants.DAY);
      eventInfo.setType(Type.SYSTEM);
      eventInfo.setLatitude(Constants.HQ_LATITUDE);
      eventInfo.setLongitude(Constants.HQ_LONGITUDE);

      eventManager.postEvent(eventInfo, topic, admin);
   }

   /**
    * Send an administration comment.
    *
    * @return next view
    * @throws Exception for any error
    */
   @Security
   public String submitComment() throws Exception
   {
      Event event = eventDAO.find(getLong("system", "eventId"));
      if (event == null)
         throw new IllegalArgumentException("No such event");

      Client admin = getAdmin();

      Comment comment = new Comment();
      comment.setTopicId(event.getTopicId());
      comment.setEventId(event.getId());
      comment.setUsername(admin.getUsername());
      comment.setPublisherId(admin.getId());
      comment.setComment(getParameter("system", "comment"));
      commentDAO.save(comment);

      return HOME;
   }

   private static String trim(String value)
   {
      if (value == null)
         return null;

      String trimmed = value.trim();
      return (trimmed.length() == 0) ? null : trimmed;
   }

   @Inject
   public void setClientDAO(ClientDAO clientDAO)
   {
      this.clientDAO = clientDAO;
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }

   @Inject
   public void setEventDAO(EventDAO eventDAO)
   {
      this.eventDAO = eventDAO;
   }

   @Inject
   public void setCommentDAO(CommentDAO commentDAO)
   {
      this.commentDAO = commentDAO;
   }

   @Inject
   public void setEventManager(EventManager eventManager)
   {
      this.eventManager = eventManager;
   }
}
