package com.alterjoc.radar.server.dao;

import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Event;
import org.jboss.capedwarf.server.api.dao.GenericDAO;

import java.util.List;

/**
 * Event DAO.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface EventDAO extends GenericDAO<Event>
{
   /**
    * Find all events for a topic past timestamp.
    *
    * @param topicId the topic id
    * @param timestamp the timestamp
    * @return events list
    */
   List<Event> findEventsTs(long topicId, long timestamp);

   /**
    * Find all events for a client past the timestamp.
    *
    * @param client the client
    * @param timestamp the timestamp
    * @return events list
    */
   List<Event> findEvents(Client client, long timestamp);

   /**
    * Find events not older than history.
    *
    * @param history the history
    * @return event list
    */
   List<Event> findEvents(long history);

   /**
    * Count created events.
    *
    * @param clientId the client id
    * @param topicId the topic id
    * @param timestamp the timestamp
    * @return number of created events
    */
   long countCreatedEvents(long clientId, long topicId, long timestamp);
}
