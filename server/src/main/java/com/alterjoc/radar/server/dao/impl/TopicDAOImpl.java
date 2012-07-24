package com.alterjoc.radar.server.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.server.dao.TopicDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Comment;
import com.alterjoc.radar.server.domain.Subscription;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.jpa.ProxyingEnum;
import org.jboss.capedwarf.server.api.cache.Cacheable;
import org.jboss.capedwarf.server.api.cache.SingleKeyStrategy;
import org.jboss.capedwarf.server.api.dao.impl.TimestampedListKeyStrategy;
import org.jboss.capedwarf.server.api.persistence.Proxying;
import org.jboss.capedwarf.server.api.tx.TransactionPropagationType;
import org.jboss.capedwarf.server.api.tx.Transactional;

/**
 * Topic DAO impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopicDAOImpl extends HackDAO<Topic> implements TopicDAO
{
   protected Class<Topic> entityClass()
   {
      return Topic.class;
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public Topic findSystemTopic()
   {
      Query query = getEM().createQuery("select t from Topic t where t.type = :type");
      query.setParameter("type", Type.SYSTEM);
      return getSingleResult(query);
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Cacheable(name = "TopicsCache", key = SingleKeyStrategy.class)
   @Proxying(ProxyingEnum.DISABLE)
   public Topic findTopic(String kind)
   {
      Query query = getEM().createQuery("select t from Topic t where t.kind = :kind");
      query.setParameter("kind", kind);
      return getSingleResult(query);
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public long countSubscribed(long topicId)
   {
      Query query = getEM().createQuery("select count(s) from Subscription s where s.topicId = :tid");
      query.setParameter("tid", topicId);
      return getCount(query);
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public List<Number> countSubscribed(List<Long> topicIds)
   {
      if (topicIds == null || topicIds.isEmpty())
         return Collections.emptyList();

      List<Number> result = new ArrayList<Number>();
      for (Long topicId : topicIds)
      {
         Query query = getEM().createQuery("select count(s) from Subscription s where s.topicId = :tid");
         query.setParameter("tid", topicId);
         result.add(getCount(query));
      }
      return result;
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   public List<Topic> findSubscribed(Client client)
   {
      List<Topic> topics = new ArrayList<Topic>();
      Set<Subscription> subscriptions = client.getSubscriptions();
      for (Subscription s : subscriptions)
      {
         Topic topic = s.getTopic();
         if (topic != null)
            topics.add(topic);
      }
      return topics;
   }

   @SuppressWarnings({"unchecked"})
   @Override
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public List<Topic> findAll()
   {
      Query query = getEM().createQuery("select t from Topic t where t.active = true order by t.timestamp");
      return query.getResultList();
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   public List<Topic> findNewTopics()
   {
      Query query = getEM().createQuery("select t from Topic t where t.confirmedId is null order by t.timestamp");
      return query.getResultList();
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   @Cacheable(name = "TopicsCache", key = AllTopicsKeyStrategy.class)
   public List<Topic> findAllSinceTs(long timestamp)
   {
      Query query = getEM().createQuery("select t from Topic t where t.active = true and t.timestamp > :ts order by t.timestamp");
      query.setParameter("ts", timestamp);
      return query.getResultList();
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public long countCreatedTopics(long clientId, long timestamp)
   {
      Query query = getEM().createQuery("select count(t) from Topic t where t.creatorId = :cid and t.timestamp > :ts");
      query.setParameter("cid", clientId);
      query.setParameter("ts", timestamp);
      return getCount(query);
   }

   @SuppressWarnings({"unchecked"})
   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   @Cacheable(name = "TopicsCache", key = TopicsKeyStrategy.class)
   @TimestampedListKeyStrategy.Prefix(Constants.TOPIC_COMMENTS)
   public List<Comment> findComments(long topicId, long timestamp)
   {
      Query query = getEM().createQuery("select c from Comment c where c.topicId = :tid and c.timestamp > :ts order by c.timestamp");
      query.setParameter("tid", topicId);
      query.setParameter("ts", timestamp);
      return query.getResultList();
   }
}
