package com.alterjoc.test.client.server.test;

import java.util.List;

import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.junit.Assert;
import org.junit.Test;

public class SyncCommentsTest extends AbstractServerTest
{
   @Test
   public void testCreateComments() throws Exception
   {
      ServerProxy proxy = getServerProxy();

      String user = "synctestuser";
      String pass = "passpass";

      // try to log in as test user
      // if user exists we use existing one, otherwise we create a new one
      boolean rval = ensureUser(proxy, user, pass);
      Assert.assertTrue("Ensure user: " + user, rval);

      // Get all topics, subscribe user to topic 'Dober žur'
      TopicInfo zurTopic = null;
      List<TopicInfo> topics = proxy.topicFindAllSinceTs(0);
      for (TopicInfo topic: topics)
      {
         if ("Dober žur".equals(topic.getName()))
         {
            zurTopic = topic;
            proxy.profileAddTopic(zurTopic.getId());            
            break;
         }
      }

      Assert.assertNotNull("zurTopic found", zurTopic);
      
      
      // Create event for the topic
      int random = (int) (Math.random()*1000000);
      String eventTitle = "Sync Comments Testing - " + random;
      EventInfo event1 = new EventInfo(eventTitle, "some comment", zurTopic.getId());
      
      StatusInfo ret = proxy.profilePostEvent(event1);
      Assert.assertEquals("profilePostEvent status == Status.OK", Status.OK, ret.getStatus());
      
      // get All events for topic - our latest one needs to be among them
      EventInfo latest = null;
      List<EventInfo> events = proxy.eventTopicEventsTs(zurTopic.getId(), 0);
      for (EventInfo ev: events)
      {
         if (eventTitle.equals(ev.getTitle()))
         {
            latest = ev;
            break;
         }
      }

      Assert.assertNotNull("latest event present", latest);
      
      // Add new comment to the event
      CommentInfo comment = new CommentInfo("Huh, that's test comment 1", zurTopic.getId(), latest.getId());
      ret = proxy.profileAddComment(comment);
      Assert.assertEquals("profileAddComment status == Status.OK", Status.OK, ret.getStatus());
      
      // Get all comments for event
      List<CommentInfo> comments = proxy.eventEventCommentsTs(latest.getId(), 0);
      Assert.assertEquals("There has to be one comment", 1, comments.size());
      
      CommentInfo retComment = comments.get(0);
      Assert.assertEquals("Comment eventId check", comment.getEventId(), retComment.getEventId());
      Assert.assertEquals("Comment text check", comment.getComment(), retComment.getComment());
      
      long timestamp = retComment.getTimestamp();
      
      // Add new comment to the event
      comment = new CommentInfo("Another test comment", zurTopic.getId(), latest.getId());
      ret = proxy.profileAddComment(comment);
      Assert.assertEquals("profileAddComment status == Status.OK", Status.OK, ret.getStatus());
      
      // Get all comments for the topic
      retComment = null;
      comments = proxy.topicTopicCommentsTs(zurTopic.getId(), timestamp);
      for (CommentInfo c: comments)
      {
         if (c.getEventId() == latest.getId())
         {
            Assert.assertTrue("timestamp check", c.getTimestamp() > timestamp);
            Assert.assertNull("We should not get back more than one comment for our event", retComment);
            retComment = c;
         }
      }
      Assert.assertNotNull("We should get back exactly one comment for our event", retComment);      
   }
}
