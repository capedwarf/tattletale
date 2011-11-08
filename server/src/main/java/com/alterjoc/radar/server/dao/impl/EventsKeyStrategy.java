package com.alterjoc.radar.server.dao.impl;

import javax.cache.CacheException;

import com.alterjoc.radar.server.domain.Event;
import org.jboss.capedwarf.server.api.cache.CacheConfig;
import org.jboss.capedwarf.server.api.dao.impl.TimestampedListKeyStrategy;

/**
 * Comments cache key.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EventsKeyStrategy extends TimestampedListKeyStrategy<Event>
{
   public EventsKeyStrategy(CacheConfig config) throws CacheException
   {
      super(config);
   }

   protected Class<Event> getEntityClass()
   {
      return Event.class;
   }
}


