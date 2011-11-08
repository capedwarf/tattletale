package com.alterjoc.radar.server.dao;

import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.domain.Subscription;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.server.api.dao.GenericDAO;

/**
 * Subscription DAO.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface SubscriptionDAO extends GenericDAO<Subscription>
{
   /**
    * Find the subscription
    *
    * @param client the client
    * @param topic the topic
    * @return the subscription
    */
   Subscription findSubscription(Client client, Topic topic);

   /**
    * Find the subscription id
    *
    * @param client the client
    * @param topic the topic
    * @return the subscription
    */
   Long idSubscription(Client client, Topic topic);

   /**
    * Does the subscription exist
    *
    * @param client the client
    * @param topic the topic
    * @return the subscription
    */
   boolean existsSubscription(Client client, Topic topic);
}
