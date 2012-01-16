package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Subscription;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.server.api.qualifiers.Current;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Find clients topics.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClientsTopicsAction extends AbstractTopicsAction
{
   private Client client;

   // TODO -- cache
   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      Set<Subscription> subscriptions = client.getSubscriptions();
      Set<Topic> topics = new HashSet<Topic>();
      for (Subscription s : subscriptions)
      {
         topics.add(s.getTopic());
      }
      toDTO(topics, resp);
   }

   @Inject
   public void setClient(@Current Client client)
   {
      this.client = client;
   }
}
