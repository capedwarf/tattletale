package com.alterjoc.radar.server.ui;

import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Event;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.common.Constants;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * UI "static" controller.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Named("ui")
@ApplicationScoped
public class UI
{
   private TopicDAO topicDAO;
   private EventDAO eventDAO;

   public List<Topic> getAllTopics()
   {
      return topicDAO.findAll();
   }

   public List<Topic> getNewTopics()
   {
      return topicDAO.findNewTopics();
   }

   public List<Event> getEvents()
   {
      return eventDAO.findEvents(Constants.THREE_DAYS);
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
}
