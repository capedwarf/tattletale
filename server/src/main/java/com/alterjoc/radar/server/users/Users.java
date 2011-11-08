package com.alterjoc.radar.server.users;

import com.alterjoc.radar.server.dao.ClientDAO;
import com.alterjoc.radar.server.domain.Client;
import org.jboss.capedwarf.server.api.quilifiers.Current;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Null;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

/**
 * Manage app user and client.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@ApplicationScoped
public class Users
{
   private ClientDAO clientDAO;

   @Produces @Current
   public Long currentClientId(HttpServletRequest request, InjectionPoint ip)
   {
      Long id = getClientId(request);
      if (id == null)
         checkClientUsage(ip);

      return id;
   }

   @Produces @Current
   public Client currentClient(HttpServletRequest request, InjectionPoint ip)         
   {
      Client client = getClient(request);
      if (client == null)
      {
         Long id = getClientId(request);
         if (id != null)
         {
            client = clientDAO.find(id);
         }
      }

      if (client == null)
         checkClientUsage(ip);

      return client;
   }

   protected void checkClientUsage(InjectionPoint ip)
   {
      Member member = ip.getMember();
      if (member instanceof AnnotatedElement)
      {
         AnnotatedElement ae = (AnnotatedElement) member;
         if (ae.isAnnotationPresent(Null.class) == false)
            throw new IllegalArgumentException("Illegal @Current Client usage: " + ip);
      }
   }

   public static Long getClientId(HttpServletRequest request)
   {
      return (Long) request.getAttribute(Client.class.getName() + ".id");
   }

   public static void setClientId(HttpServletRequest request, Long clientId)
   {
      request.setAttribute(Client.class.getName() + ".id", clientId);
   }

   public static Client getClient(HttpServletRequest request)
   {
      return (Client) request.getAttribute(Client.class.getName());
   }

   public static void setClient(HttpServletRequest request, Client client)
   {
      request.setAttribute(Client.class.getName(), client);
   }

   @Inject
   public void setClientDAO(ClientDAO clientDAO)
   {
      this.clientDAO = clientDAO;
   }
}
