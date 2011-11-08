package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.topics.TopicManager;
import org.jboss.capedwarf.server.api.mvc.BooleanAbstractAction;
import org.jboss.capedwarf.server.api.quilifiers.Current;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Add topic to client.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AddTopicToClientAction extends BooleanAbstractAction
{
   private Client client;
   private TopicManager topicManager;

   protected Boolean doHandle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String id = req.getParameter("topicId");
      if (id == null)
         throw new IllegalArgumentException("Missing topic id.");

      Long topicId = Long.parseLong(id);
      topicManager.addClientToTopic(topicId, client);
      return true;
   }

   @Inject
   public void setClient(@Current Client client)
   {
      this.client = client;
   }

   @Inject
   public void setTopicManager(TopicManager topicManager)
   {
      this.topicManager = topicManager;
   }
}
