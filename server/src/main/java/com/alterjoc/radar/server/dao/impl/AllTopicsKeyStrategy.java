package com.alterjoc.radar.server.dao.impl;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.cache.CacheException;

import com.alterjoc.radar.common.Constants;
import org.jboss.capedwarf.server.api.cache.CacheConfig;
import org.jboss.capedwarf.server.api.dao.impl.TimestampedListKeyStrategy;

/**
 * All topics cache key.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AllTopicsKeyStrategy extends TopicsKeyStrategy
{
   public AllTopicsKeyStrategy(CacheConfig config) throws CacheException
   {
      super(config);
   }

   protected int getTimestampIndex()
   {
      return 0;
   }

   public Serializable createKey(Object target, Method method, Object[] args)
   {
      return Constants.TOPICS_SINCE_TS;
   }
}


