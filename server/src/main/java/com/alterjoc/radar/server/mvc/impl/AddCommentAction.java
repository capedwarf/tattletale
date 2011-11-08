package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.server.dao.AuditLogDAO;
import com.alterjoc.radar.server.dao.CommentDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Comment;
import com.alterjoc.radar.server.domain.Event;
import com.alterjoc.radar.server.users.Limits;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.common.social.SocialEvent;
import org.jboss.capedwarf.server.api.mvc.impl.StatusInfoAbstractAction;
import org.jboss.capedwarf.server.api.quilifiers.Current;
import org.jboss.capedwarf.server.api.quilifiers.Name;

import javax.cache.Cache;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Add comment to event.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AddCommentAction extends StatusInfoAbstractAction
{
   private CommentDAO commentDAO;
   private AuditLogDAO auditLogDAO;
   private Limits limits;
   private Client client;
   private Cache topicsCache;
   private Cache commentsCache;
   private javax.enterprise.event.Event<SocialEvent> socialEvent;

   protected StatusInfo doHandle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      CommentInfo ci = deserialize(req, CommentInfo.class);

      Event event = commentDAO.find(Event.class, ci.getEventId());
      if (event != null)
      {
         if (limits.canCreateNewComment(client.getId(), event.getId()) == false)
            return new StatusInfo(Status.EXCEEDED_LIMIT);

         Comment comment = new Comment();
         comment.setTopicId(ci.getTopicId());
         comment.setEventId(ci.getEventId());
         comment.setUsername(client.getUsername());
         comment.setPublisherId(client.getId());
         comment.setComment(ci.getComment());
         commentDAO.save(comment);

         topicsCache.remove(Constants.TOPIC_COMMENTS + comment.getTopicId());
         commentsCache.remove(Constants.EVENT_COMMENTS + comment.getEventId());

         auditLogDAO.setLast(comment);

         socialEvent.fire(comment);

         StatusInfo statusInfo = new StatusInfo(Status.OK, comment.getId());
         statusInfo.setTimestamp(comment.getTimestamp());
         return statusInfo;
      }
      else
      {
         return new StatusInfo(Status.NO_SUCH_ENTITY);
      }
   }

   @Inject
   public void setCommentDAO(CommentDAO commentDAO)
   {
      this.commentDAO = commentDAO;
   }

   @Inject
   public void setAuditLogDAO(AuditLogDAO auditLogDAO)
   {
      this.auditLogDAO = auditLogDAO;
   }

   @Inject
   public void setLimits(Limits limits)
   {
      this.limits = limits;
   }

   @Inject
   public void setClient(@Current Client client)
   {
      this.client = client;
   }

   @Inject
   public void setTopicsCache(@Name("TopicsCache") Cache topicsCache)
   {
      this.topicsCache = topicsCache;
   }

   @Inject
   public void setCommentsCache(@Name("CommentsCache") Cache commentsCache)
   {
      this.commentsCache = commentsCache;
   }

   @Inject
   public void setSocialEvent(javax.enterprise.event.Event<SocialEvent> socialEvent)
   {
      this.socialEvent = socialEvent;
   }
}
