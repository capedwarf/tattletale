package com.alterjoc.test.client.server.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.Image;
import com.alterjoc.radar.common.data.PhotoInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.common.data.UserInfo;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test server proxy.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class ServerTestCase extends AbstractServerTest
{
   @Test
   public void testSmoke() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      proxy.serverTimestamp();
      proxy.eventEventCommentsTs(1, 0);
      proxy.topicCountSubscribed(1);
      proxy.eventTopicEventsTs(1, 0);
      proxy.eventOnDemandPhoto(1, Image.BIG);
   }

   @Test
   public void testSecurePost() throws Exception
   {
      if (isServerRunning() == false)
         return;

      boolean changedUser = false;
      try
      {
         ServerProxy proxy = getServerProxy();
         UserInfo user = new UserInfo(System.currentTimeMillis() + "u", "qwert123");
         LoginInfo li = proxy.profileCreateUser(user);
         Assert.assertNotNull(li);
         Assert.assertEquals(Status.OK, li.getStatus());
         setCreatedUser(li);
         changedUser = true;

         long topicId = getLastTopicId(proxy);

         EventInfo eventInfo = new EventInfo("SecureEvent", "Test event", topicId);
         StatusInfo status = proxy.profilePostEvent(eventInfo);
         long id = status.getId();
         Assert.assertTrue(id > 0);
      }
      finally
      {
         if (changedUser)
            resetAdmin(getClass());
      }
   }

   @Test
   public void testCreateUser() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      UserInfo user = new UserInfo(System.currentTimeMillis() + "X", "qwert123");
      LoginInfo li = proxy.profileCreateUser(user);
      Assert.assertNotNull(li);
      Assert.assertEquals(Status.OK, li.getStatus());
   }

   @Test
   public void testLogin() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();

      String username = System.currentTimeMillis() + "u";
      UserInfo user = new UserInfo(username, "qwert123");
      LoginInfo li = proxy.profileCreateUser(user);
      Assert.assertNotNull(li);
      Assert.assertEquals(Status.OK, li.getStatus());
      long id = li.getId();

      user = new UserInfo(username, "qwert123");
      user.setStatus(Status.LOGIN);
      li = proxy.profileCreateUser(user);
      Assert.assertNotNull(li);
      Assert.assertEquals(Status.DUPLICATE, li.getStatus());
      Assert.assertEquals(id, li.getId());
      Assert.assertNotNull(li.getToken());
   }

   @Test
   public void testReserved() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();

      UserInfo user = new UserInfo("ToZiBaBa", "qwert123");
      LoginInfo li = proxy.profileCreateUser(user);
      Assert.assertNotNull(li);
      Assert.assertEquals(Status.DUPLICATE, li.getStatus());
   }

   @Test
   public void testRecovery() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      UserInfo user = new UserInfo(System.currentTimeMillis() + "u", "qwert123");
      String email = "ales.justin@gmail.com";
      user.setEmail(email);
      LoginInfo li = proxy.profileCreateUser(user);
      Assert.assertNotNull(li);
      Assert.assertEquals(Status.OK, li.getStatus());

      // by username
      Status status = proxy.serverRecovery(user.getUsername());
      Assert.assertNotNull(status);
      Assert.assertEquals(Status.OK, status);

      // by email
      status = proxy.serverRecovery(email);
      Assert.assertNotNull(status);
      Assert.assertEquals(Status.OK, status);
   }

   @Test
   public void testAllTopics() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      List<TopicInfo> topics = proxy.topicFindAllSinceTs(0);
      Assert.assertNotNull(topics);
      Assert.assertTrue(topics.size() > 0);
   }

   @Test
   public void testNewTopic() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      TopicInfo topicInfo = new TopicInfo(System.currentTimeMillis() + "s");
      StatusInfo status = proxy.profileAddNewTopic(topicInfo);
      Assert.assertFalse(Status.ERROR.equals(status.getStatus())); // ok or exceeded
   }

   @Test
   public void testPostEvent() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      long topicId = getLastTopicId(proxy);

      EventInfo eventInfo = new EventInfo(System.currentTimeMillis() + "e", "Test event", topicId);
      CommentInfo commentInfo = new CommentInfo("qwert", topicId, 0);
      eventInfo.setUserComments(Collections.singletonList(commentInfo));
      StatusInfo status = proxy.profilePostEvent(eventInfo);
      long id = status.getId();
      Assert.assertTrue(id > 0);
   }

   @Test
   public void testAuditLog() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      long topicId = getLastTopicId(proxy);

      EventInfo eventInfo = new EventInfo(System.currentTimeMillis() + "e", "Test event", topicId);
      StatusInfo status = proxy.profilePostEvent(eventInfo);
      long id = status.getId();
      Assert.assertTrue(id > 0);

      status = proxy.profileAddComment(new CommentInfo("AuditLog", topicId, id));

      long lm = proxy.serverModification();
      Assert.assertEquals(lm, status.getTimestamp());
   }

   @Test
   public void testSubscibedTopic() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      List<TopicInfo> topics = proxy.profileSubscribedTopics();
      Assert.assertNotNull(topics);
   }

   @Test
   public void testAddRemoveTopic() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      long id = getLastTopicId(proxy);

      Long x = proxy.topicCountSubscribed(id);

      List<TopicInfo> topics = proxy.profileSubscribedTopics();
      Assert.assertNotNull(topics);
      int ts = topics.size();

      Assert.assertTrue(proxy.profileAddTopic(id));
      long x1 = proxy.topicCountSubscribed(id);
      Assert.assertEquals(x + 1, x1);

      List<Long> ids = Arrays.asList(id, -1L);
      List<Long> xs = proxy.topicCountSubscribedMulti(ids);
      Assert.assertNotNull(xs);
      Assert.assertEquals(2, xs.size());
      Assert.assertTrue(x + 1 == xs.get(0));

      topics = proxy.profileSubscribedTopics();
      Assert.assertNotNull(topics);
      Assert.assertEquals(ts + 1, topics.size());

      Assert.assertTrue(proxy.profileRemoveTopic(id));
      Long x0 = proxy.topicCountSubscribed(id);
      Assert.assertEquals(x, x0);

      topics = proxy.profileSubscribedTopics();
      Assert.assertNotNull(topics);
      Assert.assertEquals(ts, topics.size());
   }

   @Test
   public void testPhotoOnDemand() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();

      EventInfo ei = new EventInfo("Test pics", "Images ...", getLastTopicId(proxy));
      ei.setSmallPhoto("small".getBytes());
      ei.setPhoto("biggg".getBytes());
      StatusInfo si = proxy.profilePostEvent(ei);
      Assert.assertNotNull(si);
      Assert.assertEquals(Status.OK, si.getStatus());

      PhotoInfo pi = proxy.eventOnDemandPhoto(si.getId(), Image.SMALL);
      Assert.assertNotNull(pi);
      Assert.assertEquals("small", new String(pi.getPhoto()));

      pi = proxy.eventOnDemandPhoto(si.getId(), Image.BIG);
      Assert.assertNotNull(pi);
      Assert.assertEquals("biggg", new String(pi.getPhoto()));
   }

   @Test
   public void testTopicEvents() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      long topicId = getLastTopicId(proxy);
      List<EventInfo> events = proxy.eventTopicEventsTs(topicId, 0);
      Assert.assertNotNull(events);
   }

   @Test
   public void testPostEventAddComment() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      long topicId = getLastTopicId(proxy);

      EventInfo eventInfo = new EventInfo(System.currentTimeMillis() + "e", "Test event", topicId);
      CommentInfo commentInfo = new CommentInfo("qwert", topicId, 0);
      eventInfo.setUserComments(Collections.singletonList(commentInfo));
      StatusInfo status = proxy.profilePostEvent(eventInfo);
      long eventId = status.getId();
      Assert.assertTrue(eventId > 0);

      List<EventInfo> events = proxy.eventTopicEventsTs(topicId, 0);
      Assert.assertNotNull(events);
      Assert.assertTrue(events.size() > 0);
      Assert.assertNotNull(events.get(0).getUsername()); // the username should now not be null

      List<CommentInfo> previous = proxy.topicTopicCommentsTs(topicId, 0);
      Assert.assertNotNull(previous);
      int ps = previous.size();

      CommentInfo ci = new CommentInfo("qwert1", topicId, eventId);
      StatusInfo si = proxy.profileAddComment(ci);
      Assert.assertEquals(Status.OK, si.getStatus());
      ci = new CommentInfo("qwert2", topicId, eventId);
      si = proxy.profileAddComment(ci);
      Assert.assertEquals(Status.OK, si.getStatus());
      ci = new CommentInfo(":;\":;!@#$%^&**()_+-{}=\"!@#$%^&*())_::\"{}\"{}\"", topicId, eventId);
      si = proxy.profileAddComment(ci);
      Assert.assertEquals(Status.OK, si.getStatus());

      List<CommentInfo> comments = proxy.eventEventCommentsTs(eventId, 0);
      Assert.assertNotNull(comments);
      Assert.assertTrue(comments.size() > 0);

      previous = proxy.topicTopicCommentsTs(topicId, 0);
      Assert.assertNotNull(previous);
      Assert.assertEquals(ps + 3, previous.size());
   }

   @Test
   public void testPostEventWithImage() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      long topicId = getLastTopicId(proxy);

      EventInfo eventInfo = new EventInfo(System.currentTimeMillis() + "e", "Pic event", topicId);
      byte[] bigPhoto = "picture".getBytes();
      eventInfo.setPhoto(bigPhoto);
      StatusInfo status = proxy.profilePostEvent(eventInfo);
      long id = status.getId();
      Assert.assertTrue(id > 0);

      PhotoInfo pi = proxy.eventOnDemandPhoto(id, Image.BIG);
      Assert.assertNotNull(pi);
      byte[] photo = pi.getPhoto();
      Assert.assertNotNull(photo);
      Assert.assertEquals("picture", new String(photo));
   }

   @Test
   public void testCache() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();
      long topicId = getLastTopicId(proxy);

      long ts = System.currentTimeMillis();

      EventInfo eventInfo = new EventInfo(ts + "e", "Cache test 1", topicId);
      StatusInfo status = proxy.profilePostEvent(eventInfo);
      long id = status.getId();
      Assert.assertTrue(id > 0);

      List<EventInfo> events = proxy.eventTopicEventsTs(topicId, ts);
      Assert.assertNotNull(events);
      int size = events.size();
      Assert.assertTrue(size > 0);

      events = proxy.eventTopicEventsTs(topicId, ts);
      Assert.assertNotNull(events);
      size = events.size();
      Assert.assertTrue(size > 0);

      events = proxy.eventTopicEventsTs(topicId, ts + 1000);
      Assert.assertNotNull(events);
      Assert.assertTrue(events.isEmpty());

      long sts = proxy.serverTimestamp();

      eventInfo = new EventInfo(ts + "2", "Cache test 2", topicId);
      status = proxy.profilePostEvent(eventInfo);
      id = status.getId();
      Assert.assertTrue(id > 0);

      events = proxy.eventTopicEventsTs(topicId, ts);
      Assert.assertNotNull(events);
      int newSize = events.size();
      Assert.assertTrue(newSize > 0);
      Assert.assertEquals(size + 1, newSize);

      // let's just hit cache
      events = proxy.eventTopicEventsTs(topicId, ts);
      Assert.assertNotNull(events);

      events = proxy.eventTopicEventsTs(topicId, sts);
      Assert.assertNotNull(events);
      newSize = events.size();
      Assert.assertTrue(newSize == 1);

      long ts3 = proxy.serverTimestamp();

      eventInfo = new EventInfo(ts + "3", "Cache test 3", topicId);
      status = proxy.profilePostEvent(eventInfo);
      id = status.getId();
      Assert.assertTrue(id > 0);

      // let's just hit cache
      events = proxy.eventTopicEventsTs(topicId, ts3);
      Assert.assertNotNull(events);

      // older ts
      events = proxy.eventTopicEventsTs(topicId, ts);
      Assert.assertNotNull(events);
      newSize = events.size();
      Assert.assertTrue(newSize == 3);
   }

   // ------------ util methods -----

   protected long getLastTopicId(ServerProxy proxy)
   {
      List<TopicInfo> topics = proxy.topicFindAllSinceTs(0);
      Assert.assertNotNull(topics);
      Assert.assertTrue(topics.size() > 0);
      return topics.get(topics.size() - 1).getId();
   }
}
