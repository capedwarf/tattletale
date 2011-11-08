package com.alterjoc.radar.server.dao.impl;

import javax.persistence.Query;

import com.alterjoc.radar.server.dao.SubscriptionDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Subscription;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.jpa.ProxyingEnum;
import org.jboss.capedwarf.server.api.dao.impl.AbstractGenericDAO;
import org.jboss.capedwarf.server.api.persistence.Proxying;
import org.jboss.capedwarf.server.api.tx.TransactionPropagationType;
import org.jboss.capedwarf.server.api.tx.Transactional;

/**
 * Topic DAO impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SubscriptionDAOImpl extends AbstractGenericDAO<Subscription> implements SubscriptionDAO
{
   protected Class<Subscription> entityClass()
   {
      return Subscription.class;
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public Subscription findSubscription(Client client, Topic topic)
   {
      Query query = getEM().createQuery("select s from Subscription s where s.clientId = :cid and s.topicId = :tid");
      query.setParameter("cid", client.getId());
      query.setParameter("tid", topic.getId());
      return getSingleResult(query);
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public Long idSubscription(Client client, Topic topic)
   {
      Query query = getEM().createQuery("select s.id from Subscription s where s.clientId = :cid and s.topicId = :tid");
      query.setParameter("cid", client.getId());
      query.setParameter("tid", topic.getId());
      return getSingleId(query);
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public boolean existsSubscription(Client client, Topic topic)
   {
      return exists(idSubscription(client, topic));     
   }
}
