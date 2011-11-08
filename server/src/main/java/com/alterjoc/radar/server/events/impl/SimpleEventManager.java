package com.alterjoc.radar.server.events.impl;

import org.jboss.capedwarf.common.data.StatusInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Simple event manager.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@ApplicationScoped
public class SimpleEventManager extends AbstractEventManager
{
   public StatusInfo createEvent() throws IOException, ServletException
   {
      throw new IllegalArgumentException("Not supported");
   }
}
