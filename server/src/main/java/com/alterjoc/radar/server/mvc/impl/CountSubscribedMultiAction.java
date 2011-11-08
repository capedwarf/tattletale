package com.alterjoc.radar.server.mvc.impl;

import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alterjoc.radar.server.dao.TopicDAO;
import org.jboss.capedwarf.server.api.mvc.AbstractAction;

/**
 * Count subscribed.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CountSubscribedMultiAction extends AbstractAction
{
   private TopicDAO topicDAO;

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      List<Long> topicIds = parseList(req, "topicIds", Long.class);
      List<Number> counts = topicDAO.countSubscribed(topicIds);
      writeResults(resp, counts);
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }
}
