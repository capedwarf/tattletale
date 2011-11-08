package com.alterjoc.radar.client.sync;

public abstract class AbstractSynchronization implements Synchronization
{

   public Object getValue()
   {
      return null;
   }

   public void init()
   {
   }

   public void onRunFinished()
   {
   }

   abstract public void sync() throws InterruptedException;
}
