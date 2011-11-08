package com.alterjoc.radar.client;

import android.content.Context;

public class CurrentContext
{
   private static ThreadLocal<Context> current = new ThreadLocal<Context>();
   
   public static void setCurrent(Context ctx)
   {
      current.set(ctx);
   }
   
   public static Context getCurrent()
   {
      return current.get();
   }
   
   public static void cleanup()
   {
      current.remove();
   }

   public static Context failOver(Context context)
   {
      if (context != null)
         return context;
      
      return getCurrent();
   }
}
