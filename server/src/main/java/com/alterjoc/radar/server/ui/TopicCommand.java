package com.alterjoc.radar.server.ui;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Topic;
import com.alterjoc.radar.server.topics.TopicManager;
import org.jboss.capedwarf.server.api.security.Security;
import org.jboss.capedwarf.server.api.ui.Command;

/**
 * Topic command.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Named("tc")
@RequestScoped
public class TopicCommand extends Command
{
   private TopicManager topicManager;
   private TopicDAO topicDAO;
   private EventProducer eventProducer;

   @Security({"admin", "editor"})
   public void rejectTopic()
   {
      Long topicId = getLong("reject", "topicId");
      if (topicId != null)
         topicManager.rejectTopic(topicId);
      else
         throw new IllegalArgumentException("Missing topic id.");
   }

   @Security({"admin", "editor"})
   public void confirmTopic()
   {
      Long topicId = getLong("confirm", "topicId");
      if (topicId != null)
      {
         Topic topic = topicDAO.find(topicId);
         if (topic != null)
         {
            topicManager.confirmTopic(topic);
            Topic system = topicDAO.findSystemTopic();
            if (system != null)
            {
               eventProducer.submitSystemEvent(
                     system,
                     "Dodan nov kanal: " + topic.getName(),
                     topic.getDescription(),
                     null
               );
            }
         }
      }
      else
         throw new IllegalArgumentException("Missing topic id.");
   }

   @Inject
   public void setTopicManager(TopicManager topicManager)
   {
      this.topicManager = topicManager;
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }

   @Inject
   public void setEventProducer(EventProducer eventProducer)
   {
      this.eventProducer = eventProducer;
   }
}
