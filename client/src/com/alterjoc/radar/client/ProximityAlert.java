package com.alterjoc.radar.client;

import android.app.PendingIntent;

public class ProximityAlert
{
   private long eventId;
   private double latitude;
   private double longitude;
   private int radius;
   private PendingIntent intent;
   
   public ProximityAlert(Long eventId, PendingIntent intent, double latitude, double longitude, int radius)
   {
      this.eventId = eventId;
      this.intent = intent;
      this.latitude = latitude;
      this.longitude = longitude;
      this.radius = radius;
   }
   
   public double getLatitude()
   {
      return latitude;
   }
   public void setLatitude(double latitude)
   {
      this.latitude = latitude;
   }
   public double getLongitude()
   {
      return longitude;
   }
   public void setLongitude(double longitude)
   {
      this.longitude = longitude;
   }
   public int getRadius()
   {
      return radius;
   }
   public void setRadius(int radius)
   {
      this.radius = radius;
   }
   public PendingIntent getIntent()
   {
      return intent;
   }
   public void setIntent(PendingIntent intent)
   {
      this.intent = intent;
   }
   
   public long getEventId()
   {
      return eventId;
   }   
}
