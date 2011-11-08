package com.alterjoc.radar.client.sync;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.TozibabaService;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.common.data.TopicInfo;
import static com.alterjoc.radar.common.Constants.TAG_SYNC;

public class TopicSubscriptionSynchronization extends AbstractSynchronization
{
   private volatile boolean doRepeat = true;

   private Queue<TopicInfo> topics = new ConcurrentLinkedQueue<TopicInfo>();
   
   public synchronized List<TopicInfo> getAndCleanNewTopics()
   {
      List<TopicInfo> ret = new LinkedList<TopicInfo>(topics);
      topics = new ConcurrentLinkedQueue<TopicInfo>();
      return ret;
   }
   
   @Override
   public void sync() throws InterruptedException
   {
      TozibabaService svc = TozibabaService.getInstance(null);
      DBAdapter adapter = Application.getInstance().getDBHelper(svc);
      
      while (doRepeat)
      {
         doRepeat = false;
   
         Log.d(TAG_SYNC, "Fetching from localDB all topics that need subscription info synchronized with server");
         List<TopicInfo> rs = adapter.getTopicsForUpSync();
         Log.d(TAG_SYNC, "Got " + rs.size() + " topics for upsync from localDB");
         for (TopicInfo topic: rs)
         {
            if (topic.isUserSubscribed() && topic.isUserSubscribedOnSrv() == false)
            {
               boolean rval = svc.profileAddTopic(topic.getId());
               if (rval)
               {
                  topic.setUserSubscribedOnSrv(true);
                  adapter.saveEntity(topic);
                  topics.add(topic);
               }
               else
               {
                  Log.w(TAG_SYNC, "Failed to subscribe user to topic: " + topic.toShortString());
               }
            }
            else if (topic.isUserSubscribed() == false && topic.isUserSubscribedOnSrv())
            {
               boolean rval = svc.profileRemoveTopic(topic.getId());
               if (rval)
               {
                  topic.setUserSubscribedOnSrv(false);
                  adapter.saveEntity(topic);
               }
               else
               {
                  Log.w(TAG_SYNC, "Failed to unsubscribe user from topic: " + topic.toShortString());
               }            
            }
         }         
      }
   }

   public void scheduleRepeat()
   {
      doRepeat = true;      
   }   
}
