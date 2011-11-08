package com.alterjoc.radar.common.data;

/**
 * Image.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public enum Image
{
   BIG,
   SMALL;

   /**
    * Get bytes from EventInfo.
    *
    * @param ei the event info
    * @return bytes
    */
   public byte[] getBytes(EventInfo ei)
   {
      if (ei == null)
         return null;

      if (this == SMALL)
         return ei.getSmallPhoto();
      else
         return ei.getPhoto();
   }
}