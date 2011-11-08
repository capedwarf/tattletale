package com.alterjoc.radar.client.log;

import java.util.Formatter;
import java.util.concurrent.ConcurrentHashMap;

public class Log
{

   private static LogLevel defaultLevel = LogLevel.VERBOSE;
   
   private static ConcurrentHashMap<String, LogLevel> levels = new ConcurrentHashMap<String, LogLevel>(); 
   
   public static void v(String tag, String msg)
   {
      if (isLoggable(tag, LogLevel.VERBOSE))
         android.util.Log.v(tag, msg);      
   }
   
   public static void v(String tag, String msg, Throwable t)
   {
      if (isLoggable(tag, LogLevel.VERBOSE))
         android.util.Log.v(tag, msg, t);      
   }

   public static void d(String tag, String msg)
   {
      if (isLoggable(tag, LogLevel.DEBUG))
         android.util.Log.d(tag, msg);      
   }

   public static void df(String tag, String msg, Object ... params)
   {
      if (isLoggable(tag, LogLevel.DEBUG))         
         android.util.Log.d(tag, new Formatter().format(msg, params).toString());
   }

   public static void d(String tag, String msg, Throwable t)
   {
      if (isLoggable(tag, LogLevel.DEBUG))
         android.util.Log.d(tag, msg, t);      
   }
   
   public static void i(String tag, String msg)
   {
      if (isLoggable(tag, LogLevel.INFO))
      android.util.Log.i(tag, msg);
   }
   
   public static void i(String tag, String msg, Throwable t)
   {
      if (isLoggable(tag, LogLevel.INFO))
      android.util.Log.i(tag, msg, t);
   }
   
   public static void w(String tag, String msg)
   {
      if (isLoggable(tag, LogLevel.WARN))
         android.util.Log.w(tag, msg);      
   }

   public static void w(String tag, String msg, Throwable t)
   {
      if (isLoggable(tag, LogLevel.WARN))
         android.util.Log.w(tag, msg, t);      
   }

   public static void e(String tag, String msg)
   {
      if (isLoggable(tag, LogLevel.ERROR))
         android.util.Log.e(tag, msg);      
   }
   
   public static void e(String tag, String msg, Throwable t)
   {
      if (isLoggable(tag, LogLevel.ERROR))
         android.util.Log.e(tag, msg, t);      
   }

   public static boolean isLoggable(String tag, LogLevel info)
   {
      LogLevel level = levels.get(tag);
      if (level != null)
      {
         return level.getLevel() <= info.getLevel();
      }
      return defaultLevel.getLevel() <= info.getLevel();
   }
   
   /**
    * Set default LogLevel to be used if no tag specific level is set
    * 
    * @param level if null, LogLevel.SUPPRESS is used to turn off logging
    */
   public static void setDefaultLevel(LogLevel level)
   {
      if (level == null)
         level = LogLevel.SUPPRESS;
      defaultLevel = level;      
   }
 
   /**
    * Set LogLevel for specific tag
    * 
    * @param tag
    * @param level if null, tag specific setting is removed and defaultLevel is used for that tag
    */
   public static void setLogLevelFor(String tag, LogLevel level)
   {
      if (level == null)
         levels.remove(tag);      
      else
         levels.put(tag, level);
   }
 
   
}
