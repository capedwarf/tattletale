package com.alterjoc.radar.client.sync;

import com.alterjoc.radar.client.Application;

public class TidyUpProximityAlertsJob extends RunnerSynchronization
{
   
   @Override
   public void sync() throws InterruptedException
   {
      Application app = Application.getInstance();
      app.getMasterLocationListener().resetProximityAlerts(getSyncRun().getSyncRunner().getContext());
   }
}
