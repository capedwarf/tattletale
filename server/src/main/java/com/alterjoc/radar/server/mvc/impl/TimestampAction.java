package com.alterjoc.radar.server.mvc.impl;

import org.jboss.capedwarf.server.api.mvc.AbstractAction;
import org.jboss.capedwarf.server.api.utils.TimestampProvider;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Timestamp action.
 *
 * @author Ales Justin
 */
public class TimestampAction extends AbstractAction
{
   private TimestampProvider tp;

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      writeResult(resp, tp.currentTimeMillis());
   }

   @Inject
   public void setTp(TimestampProvider tp)
   {
      this.tp = tp;
   }
}
