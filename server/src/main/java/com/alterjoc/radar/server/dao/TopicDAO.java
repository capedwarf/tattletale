package com.alterjoc.radar.server.dao;

import java.util.List;

import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Comment;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.server.api.dao.GenericDAO;

/**
 * Topic DAO.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface TopicDAO extends GenericDAO<Topic>
{
   /**
    * Find system topic.
    * Take first one if there are more.
    *
    * @return the first system topic
    */
   Topic findSystemTopic();

   /**
    * Find the topic by kind.
    *
    * @param kind the kind
    * @return the topic
    */
   Topic findTopic(String kind);

   /**
    * Count subscribed on the topic.
    *
    * @param topicId the topic id
    * @return the number of subscribed users
    */
   long countSubscribed(long topicId);

   /**
    * Count subscribed on the topic.
    *
    * @param topicIds the topic id
    * @return the number of subscribed users
    */
   List<Number> countSubscribed(List<Long> topicIds);

   /**
    * Get client's subscribed topics.
    *
    * @param client the client
    * @return the client' subscribed topics
    */
   List<Topic> findSubscribed(Client client);

   /**
    * Find newly created topics.
    *
    * @return the newly created topics
    */
   List<Topic> findNewTopics();

   /**
    * Find topics since timestamp.
    *
    * @param timestamp the timestamp
    * @return the confirmed topics since id param
    */
   List<Topic> findAllSinceTs(long timestamp);

   /**
    * Count created topics since timestamp param.
    *
    * @param clientId the client id
    * @param timestamp the timestamp
    * @return number of created topics
    */
   long countCreatedTopics(long clientId, long timestamp);

   /**
    * Find all comments for topic since timestamp.
    *
    * @param topicId the topic id
    * @param timestamp the timestamp
    * @return topic's comments
    */
   List<Comment> findComments(long topicId, long timestamp);
}
