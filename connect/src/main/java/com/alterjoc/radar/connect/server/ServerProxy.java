package com.alterjoc.radar.connect.server;

import java.util.List;
import java.util.Set;

import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.Image;
import com.alterjoc.radar.common.data.PhotoInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.common.data.UserInfo;
import org.jboss.capedwarf.common.env.Secure;
import org.jboss.capedwarf.connect.server.QueryParameter;

/**
 * This class should be used to fetch data from the server or from local storage.
 *
 * @author Ales Justin
 */
public interface ServerProxy
{
   // ------------ public ----------------------

   /**
    * Get server current timestamp.
    *
    * @return the server's current timestamp
    */
   Long serverTimestamp();

   /**
    * Get last server modification timestamp.
    *
    * @return the server's last modification timestamp
    */
   Long serverModification();

   /**
    * Push for client password recovery.
    *
    * @param identification username or email
    * @return the status
    */
   Status serverRecovery(@QueryParameter("identification") String identification);

   /**
    * Count the topic subscribers.
    *
    * @param topicId the topic id
    * @return number of topic subscribers
    */
   Long topicCountSubscribed(@QueryParameter("topicId") long topicId);

   /**
    * Count the topic subscribers.
    *
    * @param topicIds the topic id
    * @return number of topic subscribers
    */
   List<Long> topicCountSubscribedMulti(@QueryParameter("topicIds") List<Long> topicIds);

   /**
    * Find all topics since timestamp param.
    *
    * @param timestamp timestamp
    * @return all topics since id param
    */
   List<TopicInfo> topicFindAllSinceTs(@QueryParameter("timestamp") long timestamp);

   /**
    * Get all comments for event past timestamp.
    *
    * @param topicId the topic id
    * @param timestamp the timestamp
    * @return the topic's comments
    */
   List<CommentInfo> topicTopicCommentsTs(@QueryParameter("topicId") long topicId, @QueryParameter("timestamp") long timestamp);

   /**
    * Get all comments for event past timestamp.
    *
    * @param topicIds the topic id
    * @param timestamp the timestamp
    * @return the topic's comments
    */
   List<CommentInfo> topicTopicCommentsTsMulti(@QueryParameter("topicIds") Set<Long> topicIds, @QueryParameter("timestamp") long timestamp);

   /**
    * Find all topic events past timestamp.
    *
    * @param topicId the topic id
    * @param timestamp the timestamp
    * @return the client events
    */
   List<EventInfo> eventTopicEventsTs(@QueryParameter("topicId") long topicId, @QueryParameter("timestamp") long timestamp);

   /**
    * Find all topic events past timestamp.
    *
    * @param topicIds the topic id
    * @param timestamp the timestamp
    * @return the client events
    */
   List<EventInfo> eventTopicEventsTsMulti(@QueryParameter("topicIds") Set<Long> topicIds, @QueryParameter("timestamp") long timestamp);

   /**
    * Get all comments for event past timestamp.
    *
    * @param eventId the event id
    * @param timestamp the timestamp
    * @return the event's comments
    */
   List<CommentInfo> eventEventCommentsTs(@QueryParameter("eventId") long eventId, @QueryParameter("timestamp") long timestamp);

   /**
    * Get on demand event photo.
    *
    * @param eventId the event id
    * @param type the image type
    * @return photo info
    */
   PhotoInfo eventOnDemandPhoto(@QueryParameter("eventId") long eventId, @QueryParameter("type") Image type);

   // ------------ secure ----------------------

   /**
    * Add new topic.
    *
    * @param topicInfo the topic info
    * @return status info
    */
   @Secure
   StatusInfo profileAddNewTopic(TopicInfo topicInfo);

   /**
    * Get client's subscribed topics.
    *
    * @return all subscribed topics
    */
   @Secure
   List<TopicInfo> profileSubscribedTopics();

   /**
    * Add topic to client.
    *
    * @param topicId the topic id
    * @return true if topic was added ok, false otherwise
    */
   @Secure
   Boolean profileAddTopic(@QueryParameter("topicId") long topicId);

   /**
    * Remove topic from client.
    *
    * @param topicId the topic id
    * @return true if topic was removed ok, false otherwise
    */
   @Secure
   Boolean profileRemoveTopic(@QueryParameter("topicId") long topicId);

   /**
    * Post event.
    *
    * @param event the initial event
    * @return status info
    */
   @Secure
   StatusInfo profilePostEvent(EventInfo event);

   /**
    * Create user.
    *
    * @param user the user info
    * @return login info
    */
   @Secure
   LoginInfo profileCreateUser(UserInfo user);

   /**
    * Find all client events past timestamp.
    * Use with care, as ssl might be costly.
    *
    * @param timestamp the timestamp
    * @return the client events
    */
   @Secure
   List<EventInfo> profileClientEvents(@QueryParameter("timestamp") long timestamp);

   /**
    * Add comment.
    *
    * @param comment the comment
    * @return status info
    */
   @Secure
   StatusInfo profileAddComment(CommentInfo comment);
}
