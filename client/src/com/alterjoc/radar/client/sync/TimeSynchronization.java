package com.alterjoc.radar.client.sync;

import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.connect.server.ServerProxyFactory;

public class TimeSynchronization extends AbstractSynchronization
{
   @Override
   public void sync() throws InterruptedException
   {
      Application app = Application.getInstance();
      if (app.getServerTime() == null)
      {
         long remoteTs = ServerProxyFactory.create(ServerProxy.class).serverTimestamp();
         app.initServerTime(remoteTs);
      }
   }
}
