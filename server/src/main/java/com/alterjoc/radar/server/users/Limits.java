package com.alterjoc.radar.server.users;

/**
 * App limits.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface Limits
{
   /**
    * Can the new user be created?
    *
    * @param ip the client ip
    * @return true if the new user can be created, false otherwise
    */
   boolean canCreateNewUser(String ip);

   /**
    * Can the client create new topic?
    *
    * @param clientId the client id
    * @return true if new topic can be created
    */
   boolean canCreateNewTopic(long clientId);

   /**
    * Can the client create new event for topic?
    *
    * @param clientId the client id
    * @param topicId the topic id
    * @return true if new event can be created, false otherwise
    */
   boolean canCreateNewEvent(long clientId, long topicId);

   /**
    * Can the client create new comment for event?
    *
    * @param clientId the client id
    * @param eventId the event id
    * @return true if new comment can be created, false otherwise
    */
   boolean canCreateNewComment(long clientId, long eventId);
}
