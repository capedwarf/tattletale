package com.alterjoc.radar.server.servlet;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.server.dao.ClientDAO;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.users.Users;
import org.jboss.capedwarf.server.api.servlet.AbstractRequestHandler;
import org.jboss.capedwarf.server.api.servlet.FilterHandler;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Verify client's authetification.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class VerificationFilter extends AbstractRequestHandler implements FilterHandler
{
   private ClientDAO clientDAO;

   protected void doInitialize(ServletContext context)
   {
   }

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      accepts(req, resp);
   }

   public boolean accepts(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      Client client = checkClient(req);
      if (client != null)
      {
         Users.setClient(req, client);
         return true;
      }
      else
      {
         String query = req.getQueryString();
         if (query.contains("action=create-user")) // IMPL DETAIL!!
            return true;
         else
         {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Illegal client auth.");
            return false;
         }
      }
   }

   private Client checkClient(HttpServletRequest request)
   {
      String cids = request.getHeader(Constants.CLIENT_ID);
      if (cids != null)
      {
         String token = request.getHeader(Constants.CLIENT_TOKEN);
         if (token != null)
         {
            long id = Long.parseLong(cids);
            if (id <= 0)
               return null;

            Client client = clientDAO.find(id);
            if (client != null && token.equals(client.getToken()))
            {
               Users.setClientId(request, id);
               return client;
            }
         }
      }
      return null;
   }

   @Inject
   public void setClientDAO(ClientDAO clientDAO)
   {
      this.clientDAO = clientDAO;
   }
}
