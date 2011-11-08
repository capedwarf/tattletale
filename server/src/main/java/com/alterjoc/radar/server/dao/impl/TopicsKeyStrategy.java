package com.alterjoc.radar.server.dao.impl;

import javax.cache.CacheException;

import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.server.api.cache.CacheConfig;
import org.jboss.capedwarf.server.api.dao.impl.TimestampedListKeyStrategy;

/**
 * Topics cache key.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopicsKeyStrategy extends TimestampedListKeyStrategy<Topic>
{
   public TopicsKeyStrategy(CacheConfig config) throws CacheException
   {
      super(config);
   }

   protected Class<Topic> getEntityClass()
   {
      return Topic.class;
   }
}


