package com.alterjoc.radar.client.log;

import android.util.Log;

public enum LogLevel
{
   VERBOSE(Log.VERBOSE),
   DEBUG(Log.DEBUG),
   INFO(Log.INFO),
   WARN(Log.WARN),
   ERROR(Log.ERROR),
   ASSERT(Log.ASSERT),
   SUPPRESS(1000);
   
   private int level;
   
   private LogLevel(int val)
   {
      level = val;
   }

   public int getLevel()
   {
      return level;
   }
}
