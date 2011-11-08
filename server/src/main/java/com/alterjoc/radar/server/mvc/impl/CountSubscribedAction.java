package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.dao.TopicDAO;
import org.jboss.capedwarf.server.api.mvc.AbstractAction;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Count subscribed.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CountSubscribedAction extends AbstractAction
{
   private TopicDAO topicDAO;

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      long topicId = parseLong(req, "topicId");
      long count = topicDAO.countSubscribed(topicId);
      writeResult(resp, count);
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }
}
