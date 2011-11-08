package com.alterjoc.radar.client.sync;

import java.util.LinkedList;
import java.util.List;

import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.common.data.EventInfo;

public class AutoArchiveEventsJob extends RunnerSynchronization
{

   @Override
   public void sync() throws InterruptedException
   {
      DBAdapter adapter = Application.getInstance().getDBHelper(null);
      List<EventInfo> updates = new LinkedList<EventInfo>();
      List<EventInfo> events = adapter.getEventsForArchiving();
      for (EventInfo event: events)
      {
         if (event.autoArchiveIfExpired())
         {
            updates.add(event);
         }
      }
      adapter.saveEntities(updates);
   }

}
