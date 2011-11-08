package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Event;
import org.jboss.capedwarf.server.api.quilifiers.Current;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Find client's events.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClientEventsAction extends AbstractEventsAction
{
   private EventDAO eventDAO;
   private Client client;

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      long timestamp = parseLong(req, "timestamp");
      List<Event> events = eventDAO.findEvents(client, timestamp);
      toDTO(events, resp);
   }

   @Inject
   public void setEventDAO(EventDAO eventDAO)
   {
      this.eventDAO = eventDAO;
   }

   @Inject
   public void setClient(@Current Client client)
   {
      this.client = client;
   }
}
