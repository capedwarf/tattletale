package com.alterjoc.radar.server.dao;

import com.alterjoc.radar.server.domain.Client;
import org.jboss.capedwarf.server.api.dao.GenericDAO;

/**
 * Client DAO.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ClientDAO extends GenericDAO<Client>
{
   /**
    * Get token via id.
    *
    * @param id the client id
    * @return the token
    */
   String findToken(long id);

   /**
    * Find client by username.
    *
    * @param username the username
    * @return found client or null
    */
   Client findClient(String username);

   /**
    * Find client by email.
    *
    * @param email the email
    * @return found client or null
    */
   Client findClientByEmail(String email);

   /**
    * Find client by recovery.
    *
    * @param recovery the recovery token
    * @return found client or null
    */
   Client findClientByRecovery(String recovery);
}
