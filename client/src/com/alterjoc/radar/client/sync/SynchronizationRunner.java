package com.alterjoc.radar.client.sync;

import java.util.List;
import java.util.TimerTask;

import android.content.Context;
import com.alterjoc.radar.client.log.Log;
import static com.alterjoc.radar.common.Constants.TAG_SYNC;

public class SynchronizationRunner extends TimerTask 
{
   private volatile boolean interruptFlag = false;
   
   private Context context;
   private SynchronizationRun run;
   
   public SynchronizationRunner(Context context, SynchronizationRun run)
   {
      this.context = context;
      this.run = run;
      run.setSyncRunner(this);
   }
   
   public Context getContext()
   {
      return context;      
   }
   
   public void run()
   {
      try
      {
         List<Synchronization> syncs = run.getSyncs();
         for (Synchronization sync: syncs)
         {
            sync.init();
         }
         
         try
         {
            for (Synchronization sync: syncs)
            {
               sync.sync();
            }
         }
         finally
         {
            for (Synchronization sync: syncs)
            {
               sync.onRunFinished();
            }
         }
      }
      catch (InterruptedException ex)
      {
         Log.w(TAG_SYNC, "Synchronization interrupted");
      }      
      catch (Exception ex)
      {
         Log.w(TAG_SYNC, "Synchronization failed: ", ex);
      }
   }
   
   public void checkInterrupted() throws InterruptedException
   {
      if (interruptFlag)
         throw new InterruptedException();
   }

   public void interrupt()
   {
      interruptFlag = true;
   }
}
