package com.alterjoc.radar.server.events;

import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Event;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.common.data.StatusInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Event manager.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface EventManager
{
   /**
    * Create event.
    *
    * @return status info
    * @throws IOException for anu I/O error
    * @throws ServletException for any servlet error
    */
   StatusInfo createEvent() throws IOException, ServletException;

   /**
    * Handle event creation.
    *
    * @param req the request
    * @param resp the response
    * @param eventInfo the event info
    * @param client the client
    * @return status info
    * @throws IOException for anu I/O error
    * @throws ServletException for any servlet error
    */
   StatusInfo createEvent(HttpServletRequest req, HttpServletResponse resp, EventInfo eventInfo, Client client) throws IOException, ServletException;

   /**
    * Post event.
    *
    * @param eventInfo the event info
    * @param topic the topic
    * @param client the client
    * @return new event
    */
   public Event postEvent(EventInfo eventInfo, Topic topic, Client client);
}
