package com.alterjoc.radar.server.mvc.impl;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import javax.cache.Cache;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.server.dao.ClientDAO;
import com.alterjoc.radar.server.domain.Client;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.UserInfo;
import org.jboss.capedwarf.server.api.admin.AdminManager;
import org.jboss.capedwarf.server.api.mvc.impl.StatusInfoAbstractAction;
import org.jboss.capedwarf.server.api.qualifiers.Name;
import org.jboss.capedwarf.server.api.security.SecurityProvider;

/**
 * Profile request.
 */
public class CreateUserAction extends StatusInfoAbstractAction
{
   private static final String ADMIN_PASSWORD = "CapeDwarf-Example-App-2011";

   private ClientDAO clientDAO;
   private Cache cache;
   private SecurityProvider securityProvider;
   private AdminManager adminManager;
   private Set<String> reservedUsernames;

   protected void doInitialize(ServletContext context)
   {
      super.doInitialize(context);

      // reserved usernames
      reservedUsernames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
      reservedUsernames.addAll(adminManager.getUsers("reserved"));

      //create administrator
      Client admin = clientDAO.findClient(Constants.ADMINISTRATOR);
      if (admin == null)
      {
         admin = new Client();
         admin.setUsername(Constants.ADMINISTRATOR);
         admin.setPassword(securityProvider.hash(Constants.ADMINISTRATOR, ADMIN_PASSWORD));
         admin.setEmail(adminManager.getAppAdminEmail());
         clientDAO.save(admin);
      }
   }

   protected static boolean isValid(String string)
   {
      return (string != null && string.trim().length() > 0);
   }

   protected LoginInfo doHandle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      UserInfo user = deserialize(req, UserInfo.class);

      String username = user.getUsername();
      if (isValid(username) == false)
         throw new IllegalArgumentException("Illegale username: " + username);
      String password = user.getPassword();
      if (isValid(password) == false)
         throw new IllegalArgumentException("Illegal password: " + password);

      LoginInfo loginInfo;

      Status status = user.getStatus();
      if (status == Status.RECOVERY)
      {
         Client recovery = clientDAO.findClientByRecovery(user.getRecovery());
         if (recovery != null && recovery.getUsername().equals(username))
         {
            recovery.setPassword(securityProvider.hash(username, password));
            String token = createToken();
            recovery.setToken(token); // new token
            recovery.setRecovery(null); // nullify recovery
            clientDAO.merge(recovery);

            cache.remove(username.toLowerCase()); // remove cached user
            
            LoginInfo li = new LoginInfo(Status.OK, recovery.getId(), token);
            li.setUsername(username);
            return li;
         }
         else
         {
            return new LoginInfo(Status.NO_SUCH_ENTITY);
         }
      }

      // check if the user is trying to use some reserved username
      if (reservedUsernames.contains(username))
         return new LoginInfo(Status.DUPLICATE);

      Client client = clientDAO.findClient(username);
      if (client != null)
      {
         String hashed = securityProvider.hash(username, password);
         boolean match = hashed.equals(client.getPassword());
         String token = match ? client.getToken() : null;
         loginInfo = new LoginInfo(Status.DUPLICATE, (token != null) ? client.getId() : 0 , token);
      }
      else if (status == Status.LOGIN)
      {
         return new LoginInfo(Status.NO_SUCH_ENTITY);
      }
      else
      {
         try
         {
            String token = createToken();

            client = new Client();
            client.setUsername(username);
            client.setPassword(securityProvider.hash(username, password));
            client.setToken(token);
            String email = user.getEmail();
            // ignore zero lenght emails
            if (email != null && email.trim().length() == 0)
               email = null;
            client.setEmail(email);

            clientDAO.save(client);

            loginInfo = new LoginInfo(Status.OK, client.getId(), token);
         }
         catch (Exception e)
         {
            log.log(Level.WARNING, "Failed to create user.", e);
            loginInfo = new LoginInfo(Status.ERROR, e.getMessage());
         }
      }

      return loginInfo;
   }

   protected LoginInfo errorResult()
   {
      return new LoginInfo(Status.ERROR);
   }

   /**
    * Create token.
    *
    * @return new token
    */
   protected String createToken()
   {
      return UUID.randomUUID().toString();
   }

   @Inject
   public void setClientDAO(ClientDAO clientDAO)
   {
      this.clientDAO = clientDAO;
   }

   @Inject
   public void setCache(@Name("ClientsCache") Cache cache)
   {
      this.cache = cache;
   }

   @Inject
   public void setSecurityProvider(SecurityProvider securityProvider)
   {
      this.securityProvider = securityProvider;
   }

   @Inject
   public void setAdminManager(AdminManager adminManager)
   {
      this.adminManager = adminManager;
   }
}
