package com.alterjoc.test.radar.server.support;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.server.domain.Event;
import org.jboss.capedwarf.common.serialization.SerializatorFactory;
import org.jboss.capedwarf.server.api.domain.GeoPt;

/**
 * Mock mapper servlet.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class MockInitialPingServlet extends HttpServlet
{
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      handle(req, resp);
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      handle(req, resp);
   }

   protected void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      EventInfo ping = SerializatorFactory.deserialize(req.getInputStream(), EventInfo.class);

      Event event = new Event();
      event.setTopicId(1l); // mock topic id
      GeoPt point = new GeoPt(ping.getLatitude(), ping.getLongitude());
      event.setPoint(point);
      event.setTimestamp(ping.getTimestamp());
      event.setId(123l); // should get event id here

      ping.setEventId(event.getId()); // set the event id to ping

      RequestDispatcher rd = req.getRequestDispatcher("/mapreduce/start");
      rd.forward(req, resp);
   }
}
