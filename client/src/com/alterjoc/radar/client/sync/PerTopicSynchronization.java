package com.alterjoc.radar.client.sync;

import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.dto.Timestamped;

import java.util.List;

import static com.alterjoc.radar.common.Constants.TAG_SYNC;

public abstract class PerTopicSynchronization extends TimestampedSynchronization
{
   private List<TopicInfo> topicsForSync;
   private boolean anyChangeCheck = true;

   public PerTopicSynchronization(Class<? extends Timestamped> type, long backLogTime)
   {
      super(type, backLogTime);
   }
   
   protected void setTopicsForSync(List<TopicInfo> topics)
   {
      this.topicsForSync = topics;      
   }
   
   protected List<TopicInfo> getTopicsForSync()
   {
      if (topicsForSync != null)
      {
         return topicsForSync;
      }
      return getTopicsFromDB();
   }
   
   private List<TopicInfo> getTopicsFromDB()
   {
      Log.d(TAG_SYNC, "Fetching from localDB all topics");
      List<TopicInfo> topics = getDBHelper().getSubscribedTopicsForSynch();
      Log.df(TAG_SYNC, "Got %d subscriber topics from localDB", topics.size());
      return topics;
   }
   
   protected void setAnyChangeCheck(boolean check)
   {
      this.anyChangeCheck = check;
   }

   protected boolean isAnyChangeCheck()
   {
      return anyChangeCheck;
   }
}
