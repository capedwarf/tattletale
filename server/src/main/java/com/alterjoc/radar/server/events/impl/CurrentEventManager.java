package com.alterjoc.radar.server.events.impl;

import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.server.domain.Client;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.common.serialization.JSONSerializator;
import org.jboss.capedwarf.server.api.qualifiers.Current;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Current event manager.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Current
public class CurrentEventManager extends AbstractEventManager
{
   private HttpServletRequest req;
   private HttpServletResponse resp;
   private Client client;

   public StatusInfo createEvent() throws IOException, ServletException
   {
      EventInfo ei = JSONSerializator.OPTIONAL_GZIP.deserialize(req.getInputStream(), EventInfo.class);
      return createEvent(req, resp, ei, client);
   }

   @Inject
   public void setReq(HttpServletRequest req)
   {
      this.req = req;
   }

   @Inject
   public void setResp(HttpServletResponse resp)
   {
      this.resp = resp;
   }

   @Inject
   public void setClient(@Current Client client)
   {
      this.client = client;
   }
}
