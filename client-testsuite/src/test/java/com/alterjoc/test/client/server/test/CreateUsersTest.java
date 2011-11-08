package com.alterjoc.test.client.server.test;

import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.UserInfo;
import org.junit.Assert;
import org.junit.Test;


public class CreateUsersTest extends AbstractServerTest
{
   @Test
   public void testCreateUsers() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      UserInfo userInfo = new UserInfo("admin", "radarradar");
      LoginInfo user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("admin", user);
      Assert.assertNotNull(user.getId());
      System.out.println("admin id: " + user.getId());

      userInfo = new UserInfo("joze", "radarradar");
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("joze", user);
      Assert.assertNotNull(user.getId());
      System.out.println("joze id: " + user.getId());
      
      userInfo = new UserInfo("tone", "radarradar");
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("tone", user);
      Assert.assertNotNull(user.getId());
      System.out.println("tone id: " + user.getId());
      
      userInfo = new UserInfo("zvone", "radarradar");
      user = proxy.profileCreateUser(userInfo);
      Assert.assertNotNull("zvone", user);
      Assert.assertNotNull(user.getId());
      System.out.println("zvone id: " + user.getId());
   }

}
