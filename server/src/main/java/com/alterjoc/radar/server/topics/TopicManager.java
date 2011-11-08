package com.alterjoc.radar.server.topics;

import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.common.data.StatusInfo;

/**
 * Topic creator.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface TopicManager
{
   /**
    * Initialize topics.
    */
   void initializeTopics();

   /**
    * Create topic.
    *
    * @param topicInfo the topic name
    * @param client the client
    * @return new topic
    */
   StatusInfo createTopic(TopicInfo topicInfo, Client client);

   /**
    * Add client to topic.
    *
    * @param topic the topic
    * @param client the client
    */
   void addClientToTopic(Topic topic, Client client);

   /**
    * Add client to topic.
    *
    * @param topic the topic
    * @param client the client
    */
   void removeClientFromTopic(Topic topic, Client client);

   /**
    * Add client to topic.
    *
    * @param topicId the topic id
    * @param client the client
    */
   void addClientToTopic(long topicId, Client client);

   /**
    * Add client to topic.
    *
    * @param topicId the topic id
    * @param client the client
    */
   void removeClientFromTopic(long topicId, Client client);

   /**
    * Reject topic.
    *
    * @param topicId the topic
    */
   void rejectTopic(long topicId);

   /**
    * Confirm topic.
    *
    * @param topicId the topic
    */
   void confirmTopic(long topicId);

   /**
    * Reject topic.
    *
    * @param topic the topic
    */
   void rejectTopic(Topic topic);

   /**
    * Confirm topic.
    *
    * @param topic the topic
    */
   void confirmTopic(Topic topic);
}
