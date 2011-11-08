package com.alterjoc.radar.server.mvc.impl;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Comment;

/**
 * Find all topics since timestamp.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopicCommentsAction extends AbstractCommentsAction
{
   private TopicDAO topicDAO;

   @SuppressWarnings({"unchecked"})
   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      long topicId = parseLong(req, "topicId");
      long timestamp = parseLong(req, "timestamp");
      List<Comment> comments = topicDAO.findComments(topicId, timestamp);
      toDTO(comments, resp);
   }

   @Inject
   public void setTopicDAO(TopicDAO topicDAO)
   {
      this.topicDAO = topicDAO;
   }
}
