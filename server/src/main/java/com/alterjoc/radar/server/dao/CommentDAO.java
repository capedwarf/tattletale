package com.alterjoc.radar.server.dao;

import com.alterjoc.radar.server.domain.Comment;
import org.jboss.capedwarf.server.api.dao.GenericDAO;

import java.util.List;

/**
 * Comment DAO.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface CommentDAO extends GenericDAO<Comment>
{
   /**
    * Find all comments for an event past the timestamp.
    *
    * @param eventId the event id
    * @param timestamp the timestamp
    * @return events list
    */
   List<Comment> findCommentsTs(long eventId, long timestamp);

   /**
    * Count created comments.
    *
    * @param clientId the client id
    * @param eventId the event id
    * @param timestamp the timestamp
    * @return number of created comments
    */
   long countCreatedComments(long clientId, long eventId, long timestamp);
}
