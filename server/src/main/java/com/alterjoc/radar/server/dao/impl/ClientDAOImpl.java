package com.alterjoc.radar.server.dao.impl;

import com.alterjoc.radar.server.dao.ClientDAO;
import com.alterjoc.radar.server.domain.Client;
import org.jboss.capedwarf.jpa.ProxyingEnum;
import org.jboss.capedwarf.server.api.cache.Cacheable;
import org.jboss.capedwarf.server.api.cache.ToLowerSingleKeyStrategy;
import org.jboss.capedwarf.server.api.dao.impl.AbstractTimestampedDAO;
import org.jboss.capedwarf.server.api.persistence.Proxying;
import org.jboss.capedwarf.server.api.tx.TransactionPropagationType;
import org.jboss.capedwarf.server.api.tx.Transactional;

import javax.persistence.Query;

/**
 * Client DAO impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ClientDAOImpl extends AbstractTimestampedDAO<Client> implements ClientDAO
{
   protected Class<Client> entityClass()
   {
      return Client.class;
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   public String findToken(long id)
   {
      Query query = getEM().createQuery("select c.token from Client c where c.id = :cid");
      query.setParameter("cid", id);
      return getSingleString(query);
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Cacheable(name = "ClientsCache", key = ToLowerSingleKeyStrategy.class)
   @Proxying(ProxyingEnum.DISABLE)
   public Client findClient(String username)
   {
      Query query = getEM().createQuery("select c from Client c where c.lowercaseUsername = :username");
      query.setParameter("username", username.toLowerCase());
      return getSingleResult(query);
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public Client findClientByEmail(String email)
   {
      Query query = getEM().createQuery("select c from Client c where c.email = :email");
      query.setParameter("email", email);
      return getSingleResult(query);
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   public Client findClientByRecovery(String recovery)
   {
      Query query = getEM().createQuery("select c from Client c where c.recovery = :recovery");
      query.setParameter("recovery", recovery);
      return getSingleResult(query);
   }
}
