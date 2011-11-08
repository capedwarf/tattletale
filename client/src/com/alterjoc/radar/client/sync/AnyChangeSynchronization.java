package com.alterjoc.radar.client.sync;

import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.connect.server.ServerProxyFactory;

import static com.alterjoc.radar.common.Constants.TAG_SYNC;


public class AnyChangeSynchronization extends TimestampedSynchronization
{

   private AnyChangeInfo anyChangeInfo = new AnyChangeInfo();
   private Long lastChange;
   private boolean changed;
   
   public AnyChangeSynchronization()
   {
      super(AnyChangeInfo.class, 0);
      anyChangeInfo.setTimestamp(-1);
   }

   @Override
   protected Timestamped getTimestampedFromLocalDB()
   {
      return anyChangeInfo;
   }

   @Override
   public void sync() throws InterruptedException
   {
      changed = false;
      getSyncRun().getSyncRunner().checkInterrupted();
      Log.d(TAG_SYNC, "Fetching from server lastModificationTs");
      try
      {
         lastChange = ServerProxyFactory.create(ServerProxy.class).serverModification();
         Log.d(TAG_SYNC, "Got lastModificationTs: " + lastChange);
      }
      catch(Exception e)
      {
         Log.w(TAG_SYNC, "IGNORED: Failed to get last server modification", e);
      }
      
      if (lastChange == null || lastChange > anyChangeInfo.getTimestamp())
         changed = true;
   }

   @Override
   public void onRunFinished()
   {
      try
      {
         Synchronization sync = getSyncRun().getSync(TopicSynchronization.class);
         if (sync != null && Boolean.TRUE.equals(sync.getValue()) == false)
            return;
         sync = getSyncRun().getSync(EventSynchronization.class);
         if (sync != null && Boolean.TRUE.equals(sync.getValue()) == false)
            return;
         sync = getSyncRun().getSync(CommentSynchronization.class);
         if (sync != null && Boolean.TRUE.equals(sync.getValue()) == false)
            return;

         long lc = lastChange != null ? lastChange : 0;
         anyChangeInfo.setTimestamp(lc);
         updateTs(lc);
      }
      catch(Exception e)
      {
         Log.e(TAG_SYNC, "AnyChangeSynchronization dispose() error: ", e);
      }
   }
   
   @Override
   public Object getValue()
   {
      return changed;
   }

   static class AnyChangeInfo extends Timestamped
   {}
}
