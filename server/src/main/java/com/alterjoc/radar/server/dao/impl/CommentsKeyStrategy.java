package com.alterjoc.radar.server.dao.impl;

import javax.cache.CacheException;

import com.alterjoc.radar.server.domain.Comment;
import org.jboss.capedwarf.server.api.cache.CacheConfig;
import org.jboss.capedwarf.server.api.dao.impl.TimestampedListKeyStrategy;

/**
 * Comments cache key.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CommentsKeyStrategy extends TimestampedListKeyStrategy<Comment>
{
   public CommentsKeyStrategy(CacheConfig config) throws CacheException
   {
      super(config);
   }

   protected Class<Comment> getEntityClass()
   {
      return Comment.class;
   }
}


