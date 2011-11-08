package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.server.api.quilifiers.Current;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Subscribed topics.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SubscribedTopicsAction extends AbstractTopicsAction
{
   private TopicDAO topicDAO;
   private Client client;

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      List<Topic> topics = topicDAO.findSubscribed(client);
      toDTO(topics, resp);      
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }

   @Inject
   public void setClient(@Current Client client)
   {
      this.client = client;
   }
}
