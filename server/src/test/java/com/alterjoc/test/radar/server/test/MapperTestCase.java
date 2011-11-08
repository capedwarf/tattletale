package com.alterjoc.test.radar.server.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.server.domain.Event;
import com.alterjoc.test.radar.server.AbstractTest;
import com.alterjoc.test.radar.server.support.MockInitialPingServlet;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.api.RunModeType;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.common.env.EnvironmentFactory;
import org.jboss.capedwarf.common.serialization.SerializatorFactory;
import org.jboss.capedwarf.common.social.SocialEvent;
import org.jboss.capedwarf.common.sql.SQLObject;
import org.jboss.capedwarf.jpa.Entity;
import org.jboss.capedwarf.server.api.domain.GeoPt;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test mapper.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Run(RunModeType.AS_CLIENT)
public class MapperTestCase extends AbstractTest
{
   @Deployment
   public static Archive createTestArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "simple.war");

      webArchive.addPackage(MockInitialPingServlet.class.getPackage()); // mock

      webArchive.addPackage(EnvironmentFactory.class.getPackage()); // env
      webArchive.addPackage(EventInfo.class.getPackage()); // data
      webArchive.addPackage(Timestamped.class.getPackage()); // data
      webArchive.addPackage(Entity.class.getPackage()); // domain
      webArchive.addPackage(Event.class.getPackage()); // domain
      webArchive.addClass(SocialEvent.class); // social data
      webArchive.addClass(SQLObject.class); // data

      webArchive.addPackage(SerializatorFactory.class.getPackage()); // serializator
      webArchive.addClass(JSONObject.class); // json
      webArchive.addClass(JSONObject.class.getName() + "$Null"); // json
      webArchive.addClass(JSONArray.class); // json
      webArchive.addClass(JSONTokener.class); // json
      webArchive.addClass(JSONException.class); // json
      webArchive.addClass(GeoPt.class); // geopt

      URL webXml = getResource("/mapper/WEB-INF/web.xml");
      webArchive.setWebXML(webXml);
      // appengine config
      URL appEngineXml = getResource("/appengine/WEB-INF/appengine-web.xml");
      webArchive.addWebResource(appEngineXml, "appengine-web.xml");
      URL logging = getResource("/appengine/WEB-INF/logging.properties");
      webArchive.addWebResource(logging, "logging.properties");
      return webArchive;
   }

   @Test
   public void testBasicMapper() throws Exception
   {
      URL url = new URL("http://localhost:8080/client/event-initial");
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true);
      conn.setDoInput(true);
      OutputStream out = conn.getOutputStream();
      EventInfo ping = new EventInfo();
      ping.setTopicId(1L);
      out.write(SerializatorFactory.serialize(ping));
      out.flush();
      out.close();
      InputStream in = conn.getInputStream();
      int ch;
      System.out.print("Result >> ");
      while ((ch = in.read()) >= 0)
      {
         System.out.print((char) ch);
      }
      in.close();
   }
}
