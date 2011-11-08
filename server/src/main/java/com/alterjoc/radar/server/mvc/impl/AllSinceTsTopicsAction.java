package com.alterjoc.radar.server.mvc.impl;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Topic;

/**
 * Find all topics since timestamp.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AllSinceTsTopicsAction extends AbstractTopicsAction
{
   private TopicDAO topicDAO;

   @SuppressWarnings({"unchecked"})
   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      long timestamp = parseLong(req, "timestamp");
      List<Topic> topics = topicDAO.findAllSinceTs(timestamp);
      toDTO(topics, resp);
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }
}
