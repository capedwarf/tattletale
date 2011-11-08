package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.dao.ClientDAO;
import com.alterjoc.radar.server.domain.Client;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.server.api.mail.MailManager;
import org.jboss.capedwarf.server.api.mvc.ResultAbstractAction;
import org.jboss.capedwarf.server.api.quilifiers.Name;

import javax.cache.Cache;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;

/**
 * Recovery actions.
 *
 * @author Ales Justin
 */
public class RecoveryAction extends ResultAbstractAction<Status>
{
   private ClientDAO clientDAO;
   private MailManager mailManager;
   private Cache cache;

   protected Status doHandle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      String identification = getParameter(req, "identification");
      Client client = clientDAO.findClient(identification);
      if (client == null)
         client = clientDAO.findClientByEmail(identification);

      if (client != null)
      {
         String email = client.getEmail();
         if (email == null || email.trim().length() == 0)
            return Status.INVALID_EMAIL;

         String token = createToken();
         client.setRecovery(token);
         clientDAO.merge(client);

         String username = client.getUsername();         
         cache.remove(username.toLowerCase());

         mailManager.sendEmailFromAdmin(
               "Tožibaba - pozabljeno geslo",
               "Uporabnik: " + username + "\n" + "Enkratno geslo: " + token,
               email
         );

         return Status.OK;
      }
      else
      {
         return Status.NO_SUCH_ENTITY;
      }
   }

   protected Status errorResult()
   {
      return Status.ERROR;
   }

   /**
    * Create token.
    *
    * @return new token
    */
   protected String createToken()
   {
      return String.valueOf(new Random().nextInt(1000000000));
   }

   @Inject
   public void setClientDAO(ClientDAO clientDAO)
   {
      this.clientDAO = clientDAO;
   }

   @Inject
   public void setMailManager(MailManager mailManager)
   {
      this.mailManager = mailManager;
   }

   @Inject
   public void setCache(@Name("ClientsCache") Cache cache)
   {
      this.cache = cache;
   }
}
