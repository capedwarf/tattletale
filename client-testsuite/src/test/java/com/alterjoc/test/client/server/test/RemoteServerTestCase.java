package com.alterjoc.test.client.server.test;

import com.alterjoc.radar.connect.server.ServerProxy;
import com.alterjoc.test.client.server.support.RadarConfiguration;
import org.jboss.capedwarf.connect.config.Configuration;
import org.junit.BeforeClass;

/**
 * Test remote server.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class RemoteServerTestCase extends ServerTestCase
{
   protected static ServerTestDelegate createDelegate()
   {
      return new ServerTestDelegate()
      {
         public String server()
         {
            return "http://mobi-radar.appspot.com/";
         }

         public Configuration<ServerProxy> configuration()
         {
            return new RadarConfiguration(false);
         }

         public String username()
         {
            return System.currentTimeMillis() + "u";
         }

         public String password()
         {
            return "MR2010_admin";
         }
      };
   }

   @BeforeClass
   public static void setUpRemote() throws Exception
   {
      resetDelegate();
      prepareAdmin(RemoteServerTestCase.class);
   }
}
