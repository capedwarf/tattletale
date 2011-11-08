package com.alterjoc.radar.client.sync;

import java.util.Collections;
import java.util.List;

public class SynchronizationRun
{
   private SynchronizationRunner runner;
   private List<Synchronization> syncs = Collections.EMPTY_LIST;
   
   public void setSyncRunner(SynchronizationRunner runner)
   {
      this.runner = runner;
   }

   public SynchronizationRunner getSyncRunner()
   {
      return runner;
   }
   
   public void setSynchronizations(List<Synchronization> syncs)
   {
      this.syncs = Collections.unmodifiableList(syncs);
      for (Synchronization sync: syncs)
      {
         if (sync instanceof RunnerSynchronization)
         {
            ((RunnerSynchronization) sync).setSyncRun(this);
         }
      }
   }

   public List<Synchronization> getSyncs()
   {
      return syncs;
   }
   
   public Synchronization getSync(Class<?> clazz)
   {
      for (Synchronization sync: syncs)
      {
         if (clazz.isAssignableFrom(sync.getClass()))
            return sync;
      }
      return null;
   }
}
