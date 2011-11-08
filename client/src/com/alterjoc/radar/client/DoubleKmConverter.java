package com.alterjoc.radar.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DoubleKmConverter
{   
   public static String toKmString(String val)
   {
      val = val.replace(',', '.');
      double dbl = Double.valueOf(val);
      DecimalFormat fmt = new DecimalFormat("##0.###", new DecimalFormatSymbols(new Locale("sl", "SI")));
      return fmt.format(dbl);
   }
   
   public static double fromValueStringToKmDouble(String val)
   {
      val = val.replace(',', '.');
      return Double.parseDouble(val);
   }

   public static String toUnitString(String val)
   {
      val = val.replace(',', '.');
      double dbl = Double.valueOf(val);
      if (dbl < 1)
      {
         return String.valueOf((int) (dbl * 1000)) + " m";
      }
      else
      {
         DecimalFormat fmt = new DecimalFormat("##0.###", new DecimalFormatSymbols(new Locale("sl", "SI")));
         return fmt.format(dbl) + " km";
      }
   }
}
