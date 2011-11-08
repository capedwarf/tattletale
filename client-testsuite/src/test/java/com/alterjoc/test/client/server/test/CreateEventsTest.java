package com.alterjoc.test.client.server.test;

import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class CreateEventsTest extends AbstractServerTest
{
   @Test
   public void testCreateEvents() throws Exception
   {
      if (isServerRunning() == false)
         return;
      
      ServerProxy proxy = getServerProxy();
      TopicInfo policeTopic = null;
      TopicInfo jamTopic = null;
      TopicInfo floodTopic = null;
      TopicInfo radarTopic = null;
      
      List<TopicInfo> topics = proxy.topicFindAllSinceTs(0);
      for (TopicInfo topic: topics)
      {
         System.out.println("Topic [name: " + topic.getName() + ", id: " + topic.getId() + "]");
         if ("Policija".equals(topic.getName()))
            policeTopic = topic;
         else if ("Zastoj".equals(topic.getName()))
            jamTopic = topic;
         else if ("Poplava".equals(topic.getName()))
            floodTopic = topic;
         else if ("Tožibaba".equals(topic.getName()))
            radarTopic = topic;
      }
      
      Assert.assertNotSame("No. of topics", 0, topics.size());
      Assert.assertNotNull("Policija topic", policeTopic);
      Assert.assertNotNull("Zastoj topic", jamTopic);
      Assert.assertNotNull("Poplava topic", floodTopic);
      Assert.assertNotNull("Tožibaba topic", radarTopic);
      
      List<EventInfo> events = proxy.eventTopicEventsTs(policeTopic.getId(), 0);
      int preSize = events.size();
      
      EventInfo event = new EventInfo();            
      event.setTopicId(policeTopic.getId());
      event.setLatitude(45966685);
      event.setLongitude(14277084);
      event.setTitle("Pazi policija!");
      event.setComment("Policija je postavila radar v smeri Ljubljane.");
      
      //EnvironmentFactory.setUserId(11);
      //EnvironmentFactory.setUserToken("some_token");
      //event.setUsername("joze");      
      event.setTimestamp(System.currentTimeMillis() - (long) (Math.random() * 100000L));
      event.setAddress("Titova c. 28, Vrhnika");
            
      StatusInfo ret = proxy.profilePostEvent(event);
      System.out.println("Posted Event: " + event.getTitle() + ". Received: " + ret);

      events = proxy.eventTopicEventsTs(policeTopic.getId(), 0);
      Assert.assertEquals("policeTopic event", preSize + 1, events.size());
      
      events = proxy.eventTopicEventsTs(jamTopic.getId(), 0);
      preSize = events.size();
      
      event = new EventInfo();
      event.setTopicId(jamTopic.getId());
      event.setTitle("Huda nesreča!!");
      event.setComment("Tovornjak se je prevrnil in polil tisoč litrov piva. Nekaj ga je še v sodih.");
      event.setLatitude(45966647);
      event.setLongitude(14297124);

      //EnvironmentFactory.setUserId(12);
      //EnvironmentFactory.setUserToken("some_token");
      //event.setUsername("joze");
      event.setTimestamp(System.currentTimeMillis() - (long) (Math.random() * 100000L));
      event.setAddress("Robova cesta 5, Vrhnika");

      ret = proxy.profilePostEvent(event);
      System.out.println("Posted Event: " + event.getTitle() + ". Received: " + ret);

      events = proxy.eventTopicEventsTs(jamTopic.getId(), 0);
      Assert.assertEquals("jamTopic event", preSize + 1, events.size());
      
      events = proxy.eventTopicEventsTs(floodTopic.getId(), 0);
      preSize = events.size();
      
      event = new EventInfo();
      event.setTopicId(floodTopic.getId());
      event.setTitle("Poplava na tvoji desni");
      event.setComment("Prevrnil sem šalico in naredil pravo poplavo.\nLe kaj bo z mano sedaj?");
      event.setLatitude(43478304);
      event.setLongitude(14178524);

      //EnvironmentFactory.setUserId(13);
      //EnvironmentFactory.setUserToken("some_token");      
      //event.setUsername("tone");
      event.setTimestamp(System.currentTimeMillis() - (long) (Math.random() * 10000L));
      event.setAddress("Cesta dveh cesarjev 21, Ljubljana");

      ret = proxy.profilePostEvent(event);
      System.out.println("Posted Event: " + event.getTitle() + ". Received: " + ret);

      events = proxy.eventTopicEventsTs(floodTopic.getId(), 0);
      Assert.assertEquals("floodTopic event", preSize + 1, events.size());

      /*
       -- can't create new EventInfos for topic 'Tozibaba' any more ...
      event = new EventInfo();
      event.setTopicId(radarTopic.getId());
      event.setTitle("Nova različica na voljo!");
      event.setComment("Na svetlo smo dali novo različico aplikacije Tozibaba.\nVečjo srečo za vesoljni svet si težko predstavljamo.\nBodi prvi in downloadaj takoj!");
      //event.setLatitude(43488304);
      //event.setLongitude(14188524);
      event.setUsername("admin");
      event.setTimestamp(System.currentTimeMillis() - (long) (Math.random() * 100000L));
      //event.setAddress("Tozibaba HQ, Tehnološki park Jug 1, Ljubljana");
      ret = proxy.profilePostEvent(event);
      System.out.println("Posted Event: " + event.getTitle() + ". Received: " + ret);

      List<EventInfo> radarEvents = proxy.eventTopicEventsId(radarTopic.getId(), 1);
      Assert.assertTrue("radarEvents size() > 0", radarEvents.size() > 0);

      */
      

/*      
      List<CommentInfo> comments = new ArrayList<CommentInfo>();
      CommentInfo comment = new CommentInfo("test comment 1", "Tone 1", 1);
      comments.add(comment);
      comment = new CommentInfo("test comment 2", "Tone 2", 2);
      comments.add(comment);
      event.setUserComments(comments);
      dbHelper.saveEntity(event, false);
*/


   }

}
