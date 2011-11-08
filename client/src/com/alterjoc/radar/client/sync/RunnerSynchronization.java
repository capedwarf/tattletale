package com.alterjoc.radar.client.sync;

public abstract class RunnerSynchronization extends AbstractSynchronization
{
   private SynchronizationRun syncRun;
   
   public SynchronizationRun getSyncRun()
   {
      return syncRun;
   }

   public void setSyncRun(SynchronizationRun syncRun)
   {
      this.syncRun = syncRun;
   }
   
   
}
