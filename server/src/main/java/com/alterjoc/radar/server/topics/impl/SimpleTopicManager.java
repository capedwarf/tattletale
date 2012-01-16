package com.alterjoc.radar.server.topics.impl;

import java.io.IOException;
import java.util.logging.Logger;
import javax.cache.Cache;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.server.dao.AuditLogDAO;
import com.alterjoc.radar.server.dao.SubscriptionDAO;
import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Subscription;
import com.alterjoc.radar.server.domain.Topic;
import com.alterjoc.radar.server.topics.TopicManager;
import org.jboss.capedwarf.common.Constants;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.server.api.io.BlobService;
import org.jboss.capedwarf.server.api.io.ResourceReader;
import org.jboss.capedwarf.server.api.mail.MailManager;
import org.jboss.capedwarf.server.api.qualifiers.Name;
import org.jboss.capedwarf.server.api.tx.Transactional;
import org.jboss.capedwarf.server.api.utils.TimestampProvider;

/**
 * Simple topic creator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@ApplicationScoped
public class SimpleTopicManager implements TopicManager
{
   protected Logger log = Logger.getLogger(getClass().getName());

   private ResourceReader resourceReader;
   private TopicDAO topicDAO;
   private SubscriptionDAO subscriptionDAO;
   private AuditLogDAO auditLogDAO;
   private MailManager mailManager;
   private Cache topicsCache;
   private TimestampProvider tp;
   private BlobService blobService;

   /**
    * Get topic kind.
    *
    * @param name the topic name
    * @return the topic kind
    */
   protected String getKind(String name)
   {
      StringBuilder builder = new StringBuilder();
      for (char ch : name.toCharArray())
      {
         if (Character.isLetterOrDigit(ch))
            builder.append(Character.toUpperCase(ch));
      }
      return builder.toString();
   }

   /**
    * Get icon.
    *
    * @param icon the icon name
    * @return icon blob key or null if no such icon
    */
   protected String getIcon(String icon)
   {
      byte[] bytes = resourceReader.getResource("icons/" + icon + ".png");
      return storeImage(bytes);
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
         return blobService.storeBytes("image/png", bytes);
      }
      catch (IOException e)
      {
         log.warning("Cannot store image: " + e);
         return null;
      }
   }

   protected void checkTopic(String name, String icon, long expirationTime)
   {
      checkTopic(name, icon, expirationTime, Type.DEFAULT);
   }

   protected void checkTopic(String name, String icon, long expirationTime, Type type)
   {
      String kind = getKind(name);
      Topic topic = topicDAO.findTopic(kind);
      if (topic == null)
      {
         topic = new Topic();
         topic.setName(name);
         topic.setKind(kind);
         topic.setConfirmedId(1l);
         topic.setCreatorId(1l);
         topic.setActive(true);
         topic.setType(type);
         topic.setExpirationTime(expirationTime);
         topic.setImage(getIcon(icon));
         topicDAO.save(topic);
      }
   }

   public void initializeTopics()
   {
      checkTopic("Tožibaba", "tattletale", Constants.MONTH, Type.SYSTEM);
      checkTopic("Zastoj", "carjam", Constants.THREE_HOURS);
      checkTopic("Policija", "police", Constants.HOUR);
      checkTopic("Prometna nesreča", "accident", Constants.THREE_HOURS);
      checkTopic("Koncert", "concert", Constants.THREE_HOURS);
      checkTopic("Požar", "fire", Constants.DAY);
      checkTopic("Poplava", "flood", Constants.THREE_DAYS);
      checkTopic("Dober žur", "party", Constants.THREE_HOURS);
      checkTopic("Poledica", "icyroad", Constants.DAY);
      checkTopic("Kje so jurčki?", "mushroom", Constants.WEEK);
      checkTopic("Lep razgled", "sun", Constants.THREE_HOURS);
      checkTopic("Mejni prehodi", "customs", Constants.THREE_HOURS);
      checkTopic("Delo na cesti", "menatwork", Constants.WEEK);
      checkTopic("Cestne zapore", "closedroad", Constants.DAY);
      checkTopic("Akcije, popusti", "discounts", Constants.WEEK);
      checkTopic("Happy hour!", "happy", Constants.WEEK);
   }

   public StatusInfo createTopic(TopicInfo ti, Client client)
   {
      String kind = getKind(ti.getName());
      Topic topic = topicDAO.findTopic(kind);
      if (topic != null)
      {
         return new StatusInfo(Status.DUPLICATE);
      }
      else
      {
         topic = new Topic();
         topic.setName(ti.getName());
         topic.setKind(kind);
         topic.setCreatorId(client.getId());
         topic.setDescription(ti.getDescription());
         topic.setExpirationTime(ti.getExpirationDelta());
         topic.setImage(storeImage(ti.getPhoto()));
         topicDAO.save(topic);

         //noinspection unchecked
         topicsCache.put(kind, topic);

         mailManager.sendEmailToAdmins(
               "Tožibaba - nov kanal",
               "Potrdi kanal [" + topic.getId() + "]: '" + topic.getName() + "' -- uporabnik: " + client.getUsername() + "\n" +
               "* " + Constants.HTTP + Constants.HOST + "/topics.cdi"
         );

         return new StatusInfo(Status.OK, topic.getId());
      }
   }

   public void addClientToTopic(Topic topic, Client client)
   {
      if (topic == null || client == null)
         return;

      addClientToTopicInternal(topic, client);
   }

   protected void addClientToTopicInternal(Topic topic, Client client)
   {
      boolean exists = subscriptionDAO.existsSubscription(client, topic);
      if (exists == false)
      {
         Subscription subscription = new Subscription();
         subscription.setClientId(client.getId());
         subscription.setTopicId(topic.getId());
         subscriptionDAO.save(subscription);
      }
   }

   public void removeClientFromTopic(Topic topic, Client client)
   {
      if (topic == null || client == null)
         return;

      removeClientFromTopicInternal(topic, client);
   }

   protected void removeClientFromTopicInternal(Topic topic, Client client)
   {
      if (Type.SYSTEM == topic.getType())
         return;
      
      Long sid = subscriptionDAO.idSubscription(client, topic);
      if (sid != null)
      {
         subscriptionDAO.delete(sid);
      }
   }

   public void rejectTopic(Topic topic)
   {
      if (topic != null)
      {
         topicsCache.remove(topic.getKind());
         topicDAO.delete(topic.getId());
      }
   }

   public void confirmTopic(Topic topic)
   {
      if (topic != null)
      {
         topic.setConfirmedId(1l); // TODO
         topic.setActive(true);
         // update ts and exp delta
         long delta = topic.getExpirationTime() - topic.getTimestamp();
         long ts = tp.currentTimeMillis();
         topic.setTimestamp(ts);
         topic.setExpirationTime(ts + delta);

         topicDAO.merge(topic);
         // a small gap
         topicsCache.remove(com.alterjoc.radar.common.Constants.TOPICS_SINCE_TS);
         // audit
         auditLogDAO.setLast(topic);
      }
   }

   public void addClientToTopic(long topicId, Client client)
   {
      addClientToTopic(topicDAO.find(topicId), client);
   }

   public void removeClientFromTopic(long topicId, Client client)
   {
      removeClientFromTopic(topicDAO.find(topicId), client);
   }

   public void rejectTopic(long topicId)
   {
      rejectTopic(topicDAO.find(topicId));
   }

   @Transactional
   public void confirmTopic(long topicId)
   {
      confirmTopic(topicDAO.find(topicId));
   }

   @Inject
   public void setResourceReader(ResourceReader resourceReader)
   {
      this.resourceReader = resourceReader;
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }

   @Inject
   public void setSubscriptionDAO(SubscriptionDAO subscriptionDAO)
   {
      this.subscriptionDAO = subscriptionDAO;
   }

   @Inject
   public void setAuditLogDAO(AuditLogDAO auditLogDAO)
   {
      this.auditLogDAO = auditLogDAO;
   }

   @Inject
   public void setMailManager(MailManager mailManager)
   {
      this.mailManager = mailManager;
   }

   @Inject
   public void setTopicsCache(@Name("TopicsCache") Cache topicsCache)
   {
      this.topicsCache = topicsCache;
   }

   @Inject
   public void setTp(TimestampProvider tp)
   {
      this.tp = tp;
   }

   @Inject
   public void setBlobService(BlobService blobService)
   {
      this.blobService = blobService;
   }
}
