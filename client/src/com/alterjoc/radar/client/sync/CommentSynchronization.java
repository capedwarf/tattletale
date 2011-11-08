package com.alterjoc.radar.client.sync;

import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.connect.server.ServerProxyFactory;

import java.util.List;

import static com.alterjoc.radar.common.Constants.TAG_SYNC;

public class CommentSynchronization extends PerTopicSynchronization
{
   private boolean done;

   public CommentSynchronization()
   {
      super(CommentInfo.class,
            Application.getInstance().getPreferences(null).getEventBackLogMillis());
   }

   @Override
   public void sync() throws InterruptedException
   {
      done = false;
      
      if (isAnyChangeCheck())
      {
         Synchronization sync = getSyncRun().getSync(AnyChangeSynchronization.class);
         if (sync != null && Boolean.TRUE.equals(sync.getValue()) == false)
            return;
      }
      
      // perform sync
      getSyncRun().getSyncRunner().checkInterrupted();
      
      // sync the events into DB
      DBAdapter dbAdapter = getDBHelper();
      ServerProxy proxy = ServerProxyFactory.create(ServerProxy.class);
      
      List<TopicInfo> topics = getTopicsForSync();

      long maxTs = 0;
      for (TopicInfo topic: topics)
      {
         getSyncRun().getSyncRunner().checkInterrupted();
         // all comments with id > sinceId
         Log.df(TAG_SYNC, "Fetching from server comments for topic: %1$s since ts: %2$tF %<tT (%<d)", topic.toShortString(), getSinceTs());
         List<CommentInfo> res = proxy.topicTopicCommentsTs(topic.getId(), getSinceTs());
         Log.df(TAG_SYNC, "Got %d comments from server", res.size());
         maxTs = getMaxTs(res, maxTs);
         dbAdapter.updateComments(this, res);
      }
      
      // dispose
      updateTs(maxTs);
      done = true;
   }

   @Override
   protected Timestamped getTimestampedFromLocalDB()
   {
      DBAdapter dbAdapter = getDBHelper();
      return dbAdapter.getLatestCommentInfo();
   }

   @Override
   public Object getValue()
   {
      return done;
   }
}
