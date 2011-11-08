package com.alterjoc.radar.server.mvc.impl;

import org.jboss.capedwarf.server.api.mvc.impl.BasicPath2Controller;
import org.jboss.capedwarf.server.api.servlet.RequestHandler;

/**
 * Topic path 2 controller bridge.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopicPath2Controller extends BasicPath2Controller
{
   protected Class<? extends RequestHandler> getHandlerClass()
   {
      return TopicController.class;
   }
}
