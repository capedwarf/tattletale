package com.alterjoc.radar.server.utils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.alterjoc.radar.server.topics.TopicManager;

/**
 * Initialize environment.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@ApplicationScoped
public class Initialization
{
   private TopicManager topicManager;

   /**
    * Initialize topic DAO.
    */
   public void initializeTopics()
   {
      topicManager.initializeTopics();      
   }

   @Inject
   public void setTopicManager(TopicManager topicManager)
   {
      this.topicManager = topicManager;
   }
}
