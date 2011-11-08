package com.alterjoc.radar.server.mvc.impl;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import com.alterjoc.radar.server.dao.CommentDAO;
import com.alterjoc.radar.server.domain.Comment;

/**
 * Find event's comments.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EventCommentsTsAction extends AbstractCommentsAction
{
   private CommentDAO commentDAO;

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      long eventId = parseLong(req, "eventId");
      long timestamp = parseLong(req, "timestamp");

      List<Comment> comments = commentDAO.findCommentsTs(eventId, timestamp);
      toDTO(comments, resp);
   }

   @Inject
   public void setCommentDAO(CommentDAO commentDAO)
   {
      this.commentDAO = commentDAO;
   }
}
