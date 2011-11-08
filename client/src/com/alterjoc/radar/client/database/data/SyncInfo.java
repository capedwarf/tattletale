package com.alterjoc.radar.client.database.data;


import org.jboss.capedwarf.common.sql.Column;
import org.jboss.capedwarf.common.sql.SQLObject;

public class SyncInfo extends SQLObject
{
   private String className;
   private long sinceTs;

   @Column
   public String getClassName()
   {
      return className;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }

   @Column
   public long getSinceTs()
   {
      return sinceTs;
   }

   public void setSinceTs(long sinceTs)
   {
      this.sinceTs = sinceTs;
   }   
   
}
