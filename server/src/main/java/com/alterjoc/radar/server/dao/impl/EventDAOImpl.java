package com.alterjoc.radar.server.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Event;
import com.alterjoc.radar.server.domain.Subscription;
import org.jboss.capedwarf.jpa.ProxyingEnum;
import org.jboss.capedwarf.server.api.cache.Cacheable;
import org.jboss.capedwarf.server.api.dao.impl.AbstractTimestampedDAO;
import org.jboss.capedwarf.server.api.dao.impl.TimestampedListKeyStrategy;
import org.jboss.capedwarf.server.api.persistence.Proxying;
import org.jboss.capedwarf.server.api.tx.TransactionPropagationType;
import org.jboss.capedwarf.server.api.tx.Transactional;

/**
 * Event DAO impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EventDAOImpl extends AbstractTimestampedDAO<Event> implements EventDAO
{
   protected Class<Event> entityClass()
   {
      return Event.class;
   }

   @Override
   protected void saveInternal(Event entity)
   {
      super.saveInternal(entity);
      EntityManager em = getEM();
      em.flush(); // so we force id creation
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   @Cacheable(name = "EventsCache", key = EventsKeyStrategy.class)
   @TimestampedListKeyStrategy.Prefix(Constants.TOPIC_EVENTS)
   public List<Event> findEventsTs(long topicId, long timestamp)
   {
      Query query = getEM().createQuery("select e from Event e where e.topicId = :tid and e.timestamp > :ts order by e.timestamp");
      query.setParameter("tid", topicId);
      query.setParameter("ts", timestamp);      
      return query.getResultList();
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public List<Event> findEvents(Client client, long timestamp)
   {
      List<Event> events = new ArrayList<Event>();
      Set<Subscription> subscriptions = client.getSubscriptions();
      for (Subscription s : subscriptions)
      {
         long topicId = s.getTopicId();
         Query query = getEM().createQuery("select e from Event e where e.topicId = :tid and e.timestamp > :ts");
         query.setParameter("tid", topicId);
         query.setParameter("ts", timestamp);
         List temp = query.getResultList();
         events.addAll(temp);
      }
      return events;
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public List<Event> findEvents(long history)
   {
      Query query = getEM().createQuery("select e from Event e where e.timestamp > :ts order by e.timestamp");
      query.setParameter("ts", currentTimestamp() - history);
      return query.getResultList();
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public long countCreatedEvents(long clientId, long topicId, long timestamp)
   {
      Query query = getEM().createQuery("select count(e) from Event e where e.publisherId = :cid and e.topicId = :tid and e.timestamp > :ts");
      query.setParameter("cid", clientId);
      query.setParameter("tid", topicId);
      query.setParameter("ts", timestamp);
      return getCount(query);
   }
}
