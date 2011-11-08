package com.alterjoc.test.client.server.test;

import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CreateCommentsTest extends AbstractServerTest
{
   @Test
   public void testCreateComments() throws Exception
   {
      if (isServerRunning() == false)
         return;
      
      ServerProxy proxy = getServerProxy();
      
      EventInfo policeEvent = null;
      TopicInfo policeTopic = null;
      
      List<TopicInfo> topics = proxy.topicFindAllSinceTs(0);
      for (TopicInfo topic: topics)
      {
         System.out.println("Topic [name: " + topic.getName() + ", id: " + topic.getId() + "]");
         if ("Policija".equals(topic.getName()))
            policeTopic = topic;
      }
      Assert.assertNotNull("Policija topic", policeTopic);
      
      List<EventInfo> policeEvents = proxy.eventTopicEventsTs(policeTopic.getId(), 0);
      Assert.assertTrue("radarEvents size() > 0", policeEvents.size() > 0);
      for (EventInfo event: policeEvents)
      {
         System.out.println("Event [title: " + event.getTitle() + "]");
         if ("Pazi policija!".equals(event.getTitle()))
         {
            policeEvent = event;
         }
      }
      Assert.assertNotNull("Event 'Pazi policija!'", policeEvent);

      List<CommentInfo> comments = proxy.eventEventCommentsTs(policeEvent.getId(), 0);
      int preSize = comments.size();
      
      CommentInfo comment = new CommentInfo("Test comment 1", policeEvent.getTopicId(), policeEvent.getId());
      StatusInfo id = proxy.profileAddComment(comment);
      System.out.println("profileAddComment for: " + comment + " returned: " + id);

      comment = new CommentInfo("Test comment 2", policeEvent.getTopicId(), policeEvent.getId());
      id = proxy.profileAddComment(comment);
      System.out.println("profileAddComment for: " + comment + " returned: " + id);

      comments = proxy.eventEventCommentsTs(policeEvent.getId(), 0);
      Assert.assertEquals("Comments added for 'Pazi policija!'", preSize + 2, comments.size());
      for (CommentInfo item: comments)
      {
         System.out.println("CommentInfo [id: " + item.getId() + ", username: " + item.getUsername() + ", comment: " + item.getComment() +"]");
      }
   }
}
