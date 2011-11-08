package com.alterjoc.test.client.server.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.UserInfo;
import org.junit.Assert;
import org.junit.Test;


public class PassRecoveryTest extends AbstractServerTest
{

   String ID_EXTRA = "0011";
   
   private static boolean isEmpty(String val)
   {
      return val == null || val.length() == 0;
   }
   
   private void assertUser(LoginInfo user)
   {
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.OK", user.getStatus() == Status.OK);
      Assert.assertTrue("user.getId() > 0", user.getId() > 0);      
      Assert.assertTrue("user.getToken().length() > 0", user.getToken().length() > 0);
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));
   }
   
   @Test
   public void testRecovery() throws Exception
   {

      String userName1 = "recUser1" + ID_EXTRA;
      String userName2 = "recUser2" + ID_EXTRA;

      ServerProxy proxy = getServerProxy();
      UserInfo userInfo = new UserInfo(userName1, "passpass");
      LoginInfo user = proxy.profileCreateUser(userInfo);
      assertUser(user);

      // Log in as non-existent user
      userInfo = new UserInfo("xyzxyz", "passpass");
      userInfo.setStatus(Status.LOGIN);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.NO_SUCH_ENTITY", user.getStatus() == Status.NO_SUCH_ENTITY);
      Assert.assertEquals("user.getId() == 0", 0, user.getId());      
      Assert.assertTrue("user.getToken() == ''", isEmpty(user.getToken()));
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));

      // Log in as user we just created, but with wrong pass
      userInfo = new UserInfo(userName1, "wrongpass");
      userInfo.setStatus(Status.LOGIN);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.DUPLICATE", user.getStatus() == Status.DUPLICATE);
      Assert.assertEquals("user.getId() == 0", 0, user.getId());      
      Assert.assertTrue("user.getToken() == ''", isEmpty(user.getToken()));
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));

      // Log in with correct pass
      userInfo = new UserInfo(userName1, "passpass");      
      userInfo.setStatus(Status.LOGIN);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.DUPLICATE", user.getStatus() == Status.DUPLICATE);
      Assert.assertTrue("user.getId() > 0", user.getId() > 0);      
      Assert.assertTrue("user.getToken().length() > 0", user.getToken().length() > 0);
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));


      // Let's try recover the password

      // Non-existent user
      Status status = proxy.serverRecovery("xyzxyz");
      Assert.assertNotNull("status", status);
      Assert.assertTrue("status == Status.NO_SUCH_ENTITY", status == Status.NO_SUCH_ENTITY);

      // User we just created
      status = proxy.serverRecovery(userName1);
      Assert.assertNotNull("status", status);
      Assert.assertTrue("status == Status.INVALID_EMAIL", status == Status.INVALID_EMAIL);


      // Let's create a user with email
      userInfo = new UserInfo(userName2, "passpass");
      userInfo.setEmail("marko.strukelj@gmail.com");
      user = proxy.profileCreateUser(userInfo);
      assertUser(user);

      // Let's try recover the password
      status = proxy.serverRecovery(userName2);
      Assert.assertNotNull("status", status);
      Assert.assertTrue("status == Status.OK", status == Status.OK);

      System.out.println("Check email and enter a recovery password: ");
      BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
      String passKey = rd.readLine();

      // Try to recover using passKey but a wrong username
      userInfo = new UserInfo("xyzxyz", "asdasd0");
      userInfo.setRecovery(passKey);
      userInfo.setStatus(Status.RECOVERY);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.NO_SUCH_ENTITY", user.getStatus() == Status.NO_SUCH_ENTITY);
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));
      Assert.assertTrue("user.getId() == 0", user.getId() == 0);      
      Assert.assertTrue("user.getToken() == ''", isEmpty(user.getToken()));

      // Try recover using the correct username but wrong passKey 
      userInfo = new UserInfo(userName2, "asdasd1");
      userInfo.setRecovery("-1");
      userInfo.setStatus(Status.RECOVERY);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.NO_SUCH_ENTITY", user.getStatus() == Status.NO_SUCH_ENTITY);
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));
      Assert.assertTrue("user.getId() == 0", user.getId() == 0);      
      Assert.assertTrue("user.getToken() == ''", isEmpty(user.getToken()));

      // Recover using the correct username and passKey
      userInfo = new UserInfo(userName2, "asdasd");
      userInfo.setRecovery(passKey);
      userInfo.setStatus(Status.RECOVERY);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.OK", user.getStatus() == Status.OK);
      Assert.assertEquals("user.getUsername() == " + userName2, userName2, user.getUsername());
      Assert.assertTrue("user.getId() > 0", user.getId() > 0);      
      Assert.assertTrue("user.getToken().length() > 0", user.getToken().length() > 0);

      // Login with new password      
      userInfo = new UserInfo(userName2, "asdasd");
      userInfo.setStatus(Status.LOGIN);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.DUPLICATE", user.getStatus() == Status.DUPLICATE);
      Assert.assertTrue("user.getId() > 0", user.getId() > 0);      
      Assert.assertTrue("user.getToken().length() > 0", user.getToken().length() > 0);
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));

      // Login with old password
      userInfo = new UserInfo(userName2, "passpass");
      userInfo.setStatus(Status.LOGIN);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.DUPLICATE", user.getStatus() == Status.DUPLICATE);
      Assert.assertEquals("user.getId() == 0", 0, user.getId());
      Assert.assertTrue("user.getToken() == ''", isEmpty(user.getToken()));
      Assert.assertTrue("user.getUsername() == ''", isEmpty(user.getUsername()));

      // Try to use the same token again
      userInfo = new UserInfo(passKey, "asdasd");
      userInfo.setStatus(Status.RECOVERY);
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("user", user);
      Assert.assertTrue("user.getStatus() == Status.NO_SUCH_ENTITY", user.getStatus() == Status.NO_SUCH_ENTITY);
   }
}
