package com.alterjoc.radar.connect.config;

import org.jboss.capedwarf.connect.config.Configuration;

public class MobiRadarConfiguration extends Configuration
{
   public MobiRadarConfiguration()
   {
      setDebugMode(false);
      setDebugLogging(false);
      
      setHostName("mobi-radar.appspot.com");
      setSslPort(443);
      setPort(80);
   }
}