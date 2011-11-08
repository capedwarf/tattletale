package com.alterjoc.radar.common.data;


import org.jboss.capedwarf.common.sql.Column;
import org.jboss.capedwarf.common.sql.SQLObject;

/**
 * @author Dejan Pazin
 * @author Ales Justin
 */
public abstract class ImageInfo extends SQLObject
{
   private String name;

   protected ImageInfo()
   {
   }

   @Column
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public abstract void readFromBytesIntoDB(byte[] byteArray);

   public abstract byte[] readFromDBIntoArray();

   public boolean equals(Object obj)
   {
      if (super.equals(obj) == false)
         return false;

      ImageInfo other = (ImageInfo) obj;
      if (getName() == null && other.getName() == null)
         return true;

      return getName().equals(other.getName());
   }
}
