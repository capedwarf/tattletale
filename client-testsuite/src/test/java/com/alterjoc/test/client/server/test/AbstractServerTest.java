package com.alterjoc.test.client.server.test;

import com.alterjoc.radar.connect.server.ServerProxy;
import com.alterjoc.test.client.server.support.RadarConfiguration;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.UserInfo;
import org.jboss.capedwarf.common.env.EnvironmentFactory;
import org.jboss.capedwarf.connect.config.Configuration;
import org.jboss.capedwarf.connect.server.ServerProxyFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * Abstract server test.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractServerTest
{
   private static ServerTestDelegate delegate;
   private static boolean serverRunning;

   private ServerProxy proxy;

   protected ServerProxy getServerProxy()
   {
      if (proxy == null)
         throw new IllegalArgumentException("Null server proxy");

      return proxy;
   }

   @Before
   public void createServerProxy() throws Exception
   {
      proxy = create();
   }

   @After
   public void destroyServerProxy()
   {
      doDestroy();
   }

   protected ServerProxy create() throws Exception
   {
      ServerTestDelegate std = getDelegate(getClass());
      return ServerProxyFactory.create(std.configuration());
   }

/*
      return ServerProxyFactory.create(new DebugProxyHandler(std.configuration()), ServerProxy.class);
   }

   protected class DebugProxyHandler extends ServerProxyHandler {
       public X(Configuration config) {
           super(config);
       }

       @Override
       protected Object toValue(Method method, InputStream content) throws Throwable {
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           IOUtils.copyAndClose(content, baos);
           byte[] buf = baos.toByteArray();
           // System.out.println("content = " + new String(buf));
           ByteArrayInputStream stream = new ByteArrayInputStream(buf);
           return super.toValue(method, stream);
       }
   }
*/

   protected void doDestroy()
   {
      ServerProxyFactory.shutdown(ServerProxy.class);
   }

   /**
    * Create server test delegate.
    *
    * @return the server test delegate
    */
   protected static ServerTestDelegate createDelegate()
   {
      return new ServerTestDelegate()
      {
         public String server()
         {
            return "http://localhost:8080/";
         }

         public Configuration<ServerProxy> configuration()
         {
            return new RadarConfiguration(true);
         }

         public String username()
         {
            return System.currentTimeMillis() + "u";
         }

         public String password()
         {
            return "qwert123";
         }
      };
   }

   protected static ServerTestDelegate getDelegate(Class<?> testClass) throws Exception
   {
      if (delegate == null)
      {
         ServerTestDelegate tmp = null;
         Class<?> current = testClass;
         while (current != null)
         {
            try
            {
               Method createDelegate = current.getDeclaredMethod("createDelegate");
               createDelegate.setAccessible(true);
               tmp = (ServerTestDelegate) createDelegate.invoke(null);
               break;
            }
            catch (Exception ignored)
            {
            }
            current = current.getSuperclass();
         }
         if (tmp == null)
            throw new IllegalArgumentException("No such createDelegate method.");

         delegate = tmp;
      }
      return delegate;
   }

   protected static void resetDelegate()
   {
      delegate = null;
   }

   protected static void prepareAdmin(Class<?> testClass) throws Exception
   {
      checkServerRunning(testClass);

      if (isServerRunning() == false)
         return;

      resetAdmin(testClass);
   }

   protected static void resetAdmin(Class<?> testClass) throws Exception
   {
      ServerTestDelegate std = getDelegate(testClass);
      ServerProxy proxy = ServerProxyFactory.create(std.configuration());
      try
      {
         String username = std.username();
         String password = std.password();
         UserInfo user = new UserInfo(username, password);
         LoginInfo li = proxy.profileCreateUser(user);
         setCreatedUser(li);
      }
      finally
      {
         ServerProxyFactory.shutdown(ServerProxy.class);
      }
   }

   protected static void checkServerRunning(Class<?> testClass)
   {
      try
      {
         ServerTestDelegate std = getDelegate(testClass);
         URL url = new URL(std.server());
         url.openStream();
         serverRunning = true;
      }
      catch (Exception ignored)
      {
      }
   }

   @BeforeClass
   public static void setUp() throws Exception
   {
      prepareAdmin(AbstractServerTest.class);
   }

   /**
    * Is the server running.
    *
    * @return true if the server is running, false otherwise
    */
   protected static boolean isServerRunning()
   {
      return serverRunning;
   }

   /**
    * Set user from create.
    *
    * @param li the login info from createUser
    * @throws Exception for any error
    */
   protected static void setCreatedUser(LoginInfo li) throws Exception
   {
      Assert.assertNotNull(li);
      if (Status.OK == li.getStatus())
      {
         setUserId(li.getId());
         setUserToken(li.getToken());
      }
      else if (Status.DUPLICATE == li.getStatus())
      {
         Assert.assertNotNull("Wrong password.", li.getToken());
      }
      else if (Status.ERROR == li.getStatus())
      {
         Assert.fail("Error: " + li.getToken());
      }
      else
      {
         Assert.fail("username already used!");
      }
   }

   /**
    * Set user from login.
    *
    * @param li the login info from createUser
    * @throws Exception for any error
    */
   protected static void setLoggedInUser(LoginInfo li) throws Exception
   {
      Assert.assertNotNull(li);
      if (Status.DUPLICATE == li.getStatus())
      {
         Assert.assertTrue("Wrong password.", li.getId() > 0);
         setUserId(li.getId());
         setUserToken(li.getToken());
      }
      else
      {
         Assert.fail("Unexpected status!");
      }
   }

   /**
    * Set user id.
    *
    * @param id the user id
    * @throws Exception for any error
    */
   private static void setUserId(Long id) throws Exception
   {
      if (id == null)
         throw new IllegalArgumentException("User id should not be null.");

      Class<?> gec = EnvironmentFactory.class;
      Method m = gec.getDeclaredMethod("setUserId", long.class);
      m.setAccessible(true);
      m.invoke(null, id);
   }

   /**
    * Set user token.
    *
    * @param token the user token
    * @throws Exception for any error
    */
   private static void setUserToken(String token) throws Exception
   {
      Class<?> gec = EnvironmentFactory.class;
      Method m = gec.getDeclaredMethod("setUserToken", String.class);
      m.setAccessible(true);
      m.invoke(null, token);
   }

   protected static boolean userExists(ServerProxy srv, String user, String pass) throws Exception
   {
      UserInfo info = new UserInfo(user, pass);
      info.setStatus(Status.LOGIN);

      LoginInfo ret = srv.profileCreateUser(info);
      if (ret.getStatus() == Status.DUPLICATE && ret.getId() > 0)
      {
         setLoggedInUser(ret);
         return true;
      }
      return false;
   }

   protected static boolean ensureUser(ServerProxy srv, String user, String pass) throws Exception
   {
      if (userExists(srv, user, pass))
         return true;

      UserInfo info = new UserInfo(user, pass);
      LoginInfo ret = srv.profileCreateUser(info);
      return ret.getStatus() == Status.OK && ret.getId() > 0;
   }

}
