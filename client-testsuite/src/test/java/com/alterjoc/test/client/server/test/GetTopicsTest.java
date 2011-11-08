package com.alterjoc.test.client.server.test;

import java.util.List;

import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.junit.Test;

public class GetTopicsTest extends AbstractServerTest
{
   @Test
   public void testCreateComments() throws Exception
   {
      if (isServerRunning() == false)
         return;

      ServerProxy proxy = getServerProxy();

      List<TopicInfo> topics = proxy.topicFindAllSinceTs(0);
      for (TopicInfo topic: topics)
      {
         System.out.println("Topic [id: " + topic.getId() + ", name: " + topic.getName() + "]");
         byte [] photoBytes = topic.getPhoto();
         if (photoBytes != null)
            System.out.println("  -- contains photo: " + photoBytes.length + "b");
      }
   }
}
