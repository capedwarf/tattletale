package com.alterjoc.radar.client.sync;

import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.TozibabaService;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.database.data.SyncInfo;
import com.alterjoc.radar.client.log.Log;
import org.jboss.capedwarf.common.dto.Timestamped;

import java.util.List;

import static com.alterjoc.radar.common.Constants.TAG_SYNC;

public abstract class TimestampedSynchronization extends RunnerSynchronization
{
   private long sinceTs;   
   private Class<? extends Timestamped> type;
   private SyncInfo info;
   private long backLogTime;
   
   public TimestampedSynchronization(Class<? extends Timestamped> type, long backLogTime)
   {
      this.type = type;
      this.backLogTime = backLogTime;
   }  
   
   public long getSinceTs()
   {
      return sinceTs;
   }

   public void setSinceTs(long sinceTs)
   {
      if (sinceTs == 0 && backLogTime > 0)
      {
         long minusThreeDays = System.currentTimeMillis() - backLogTime;
         Application.ServerTime stime = Application.getInstance().getServerTime();
         if (stime != null)
            sinceTs = stime.localToRemote(minusThreeDays);
         else
            sinceTs = minusThreeDays;
      }
      this.sinceTs = sinceTs;
   }
   
   protected void initSinceTs()
   {
      if (getSinceTs() > 0)
         return;
      
      DBAdapter dbAdapter = getDBHelper();
      info = dbAdapter.getSyncInfo(type);
      long sinceTs = 0;
      
      if (info == null)
      {
         Timestamped timestamped = getTimestampedFromLocalDB();
         if (timestamped != null)
         {
            sinceTs = timestamped.getTimestamp();
         }
         info = new SyncInfo();
         info.setClassName(type.getName());
         info.setSinceTs(sinceTs);
         dbAdapter.putSyncInfo(info);
      }
      else
      {
         sinceTs = info.getSinceTs();
      }      
      setSinceTs(sinceTs);
   }
   
   protected void updateTs(long lastTs)
   {
      if (lastTs > sinceTs)
         updateSyncInfo(lastTs);
   }
   
   protected void updateTs(List<? extends Timestamped> vals)
   {
      long ts = sinceTs;
      for (Timestamped curr: vals)
      {
         long curTs = curr.getTimestamp();
         if (curTs > ts)
            ts = curTs;            
      }
      if (ts != sinceTs)
         updateSyncInfo(ts);
   }
   
   protected long getMaxTs(List<? extends Timestamped> vals, long maxTs)
   {
      long ts = maxTs;
      for (Timestamped curr: vals)
      {
         long curTs = curr.getTimestamp();
         if (curTs > ts)
            ts = curTs;            
      }
      
      return ts > sinceTs ? ts : sinceTs;
   }

   private void updateSyncInfo(long ts)
   {
      if (info != null && info.getSinceTs() != ts)
      {         
         info.setSinceTs(ts);
         getDBHelper().putSyncInfo(info);
      }      
   }

   public void init()
   {
      initSinceTs();
      Log.df(TAG_SYNC, "%1$s - inited sinceTs: %2$tF %<tT (%<d)", type, getSinceTs());
   }

   protected DBAdapter getDBHelper()
   {
      return Application.getInstance().getDBHelper(TozibabaService.getInstance(null));
   }   
   
   protected abstract Timestamped getTimestampedFromLocalDB();
}
