package com.alterjoc.radar.server.mvc.impl;

import org.jboss.capedwarf.common.env.Secure;
import org.jboss.capedwarf.server.api.mvc.impl.BasicPath2Controller;
import org.jboss.capedwarf.server.api.servlet.RequestHandler;

/**
 * Profile path 2 controller bridge.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Secure
public class ProfilePath2Controller extends BasicPath2Controller
{
   protected Class<? extends RequestHandler> getHandlerClass()
   {
      return ProfileController.class;
   }
}
