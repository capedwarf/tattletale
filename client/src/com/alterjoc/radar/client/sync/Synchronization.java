package com.alterjoc.radar.client.sync;

public interface Synchronization
{
   public void init();

   public void sync() throws InterruptedException;

   public void onRunFinished();

   public Object getValue();
}
