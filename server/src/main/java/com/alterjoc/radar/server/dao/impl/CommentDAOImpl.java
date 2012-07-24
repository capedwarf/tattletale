package com.alterjoc.radar.server.dao.impl;

import java.util.List;

import javax.persistence.Query;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.server.dao.CommentDAO;
import com.alterjoc.radar.server.domain.Comment;
import org.jboss.capedwarf.jpa.ProxyingEnum;
import org.jboss.capedwarf.server.api.cache.Cacheable;
import org.jboss.capedwarf.server.api.dao.impl.TimestampedListKeyStrategy;
import org.jboss.capedwarf.server.api.persistence.Proxying;
import org.jboss.capedwarf.server.api.tx.TransactionPropagationType;
import org.jboss.capedwarf.server.api.tx.Transactional;

/**
 * Comment DAO impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CommentDAOImpl extends HackDAO<Comment> implements CommentDAO
{
   protected Class<Comment> entityClass()
   {
      return Comment.class;
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   @Cacheable(name = "CommentsCache", key = CommentsKeyStrategy.class)
   @TimestampedListKeyStrategy.Prefix(Constants.EVENT_COMMENTS)
   public List<Comment> findCommentsTs(long eventId, long timestamp)
   {
      Query query = getEM().createQuery("select c from Comment c where c.eventId = :eid and c.timestamp > :ts order by c.timestamp");
      query.setParameter("eid", eventId);
      query.setParameter("ts", timestamp);
      return query.getResultList();
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public long countCreatedComments(long clientId, long eventId, long timestamp)
   {
      Query query = getEM().createQuery("select count(c) from Comment c where c.publisherId = :cid and c.eventId = :eid and c.timestamp > :ts");
      query.setParameter("cid", clientId);
      query.setParameter("eid", eventId);
      query.setParameter("ts", timestamp);
      return getCount(query);
   }
}
