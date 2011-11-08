package com.alterjoc.radar.server.users.impl;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.alterjoc.radar.server.dao.CommentDAO;
import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.users.Limits;
import org.jboss.capedwarf.server.api.utils.TimestampProvider;
import org.jboss.seam.solder.resourceLoader.Resource;

/**
 * Basic app limits.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@ApplicationScoped
public class BasicLimits implements Limits
{
   private Properties props;
   private Map<String, Long> limits = new ConcurrentHashMap<String, Long>();

   private TimestampProvider tp;

   private TopicDAO topicDAO;
   private EventDAO eventDAO;
   private CommentDAO commentDAO;

   protected Long getLimit(String key)
   {
      Long result = limits.get(key);
      if (result == null)
      {
         String no = props.getProperty(key, "0");
         String[] split = no.split("\\*");
         long tmp = 1l;
         for (String s : split)
         {
            long l = Long.parseLong(s);
            tmp *= l;
         }
         result = tmp;
         limits.put(key, result);
      }
      return result;
   }

   protected Long getNumberLimit(String key)
   {
      return getLimit(key + ".no");
   }

   protected Long getTimeLimit(String key)
   {
      return getLimit(key + ".time");
   }

   public boolean canCreateNewUser(String ip)
   {
      return true; // TODO
   }

   public boolean canCreateNewTopic(long clientId)
   {
      long time = getTimeLimit("topics");
      long timestamp = tp.currentTimeMillis() - time;
      long count = topicDAO.countCreatedTopics(clientId, timestamp);
      long no = getNumberLimit("topics");
      return count <= no;
   }

   public boolean canCreateNewEvent(long clientId, long topicId)
   {
      long time = getTimeLimit("events");
      long timestamp = tp.currentTimeMillis() - time;
      long count = eventDAO.countCreatedEvents(clientId, topicId, timestamp);
      long no = getNumberLimit("events");
      return count <= no;
   }

   public boolean canCreateNewComment(long clientId, long eventId)
   {
      long time = getTimeLimit("comments");
      long timestamp = tp.currentTimeMillis() - time;
      long count = commentDAO.countCreatedComments(clientId, eventId, timestamp);
      long no = getNumberLimit("comments");
      return count <= no;
   }

   @Inject
   public void setProps(@Resource("limits.properties") Properties props)
   {
      this.props = props;
   }

   @Inject
   public void setTp(TimestampProvider tp)
   {
      this.tp = tp;
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
}
