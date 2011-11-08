package com.alterjoc.radar.client.sync;

import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.connect.server.ServerProxyFactory;

import java.util.List;

import static com.alterjoc.radar.common.Constants.TAG_SYNC;

public class TopicSynchronization extends TimestampedSynchronization
{
   private boolean done;
   
   public TopicSynchronization()
   {
      super(TopicInfo.class, 0);
   }   

   @Override
   protected Timestamped getTimestampedFromLocalDB()
   {
      DBAdapter dbAdapter = getDBHelper();
      return dbAdapter.getLatestTopicInfo();
   }
   
   @Override
   public void sync() throws InterruptedException
   {
      done = false;
      
      Synchronization sync = getSyncRun().getSync(AnyChangeSynchronization.class);
      if (sync != null && Boolean.TRUE.equals(sync.getValue()) == false)
         return;

      // perform sync
      // all Topics with id > sinceId
      getSyncRun().getSyncRunner().checkInterrupted();
      Log.d(TAG_SYNC, "Fetching from server all topics since id: " + getSinceTs());
      List<TopicInfo> res = ServerProxyFactory.create(ServerProxy.class).topicFindAllSinceTs(getSinceTs());
      Log.d(TAG_SYNC, "Got " + res.size() + " topics from server");
      getSyncRun().getSyncRunner().checkInterrupted();
      
      // sync the events into DB
      DBAdapter dbAdapter = getDBHelper();
      dbAdapter.updateTopics(this, res);

      // cleanup
      if (res.size() > 0)
         updateTs(res);
      done = true;
   }
   
   @Override
   public Object getValue()
   {
      return done;
   }
}
