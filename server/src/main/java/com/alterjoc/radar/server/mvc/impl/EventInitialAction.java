package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.events.EventManager;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.server.api.mvc.impl.StatusInfoAbstractAction;
import org.jboss.capedwarf.server.api.quilifiers.Current;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handle initial event ping request.
 */
public class EventInitialAction extends StatusInfoAbstractAction
{
   private EventManager eventManager;

   protected StatusInfo doHandle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      return eventManager.createEvent();
   }

   @Inject
   public void setEventManager(@Current EventManager eventManager)
   {
      this.eventManager = eventManager;
   }
}
