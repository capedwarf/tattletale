package com.alterjoc.radar.server.mvc.impl;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.domain.Event;

/**
 * Find topic's events.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopicEventsTsAction extends AbstractEventsAction
{
   private EventDAO eventDAO;

   // TODO -- cache
   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      long topicId = parseLong(req, "topicId");
      long timestamp = parseLong(req, "timestamp");

      List<Event> events = eventDAO.findEventsTs(topicId, timestamp);
      toDTO(events, resp);
   }

   @Inject
   public void setEventDAO(EventDAO eventDAO)
   {
      this.eventDAO = eventDAO;
   }
}
