package com.alterjoc.radar.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.server.util.ServiceConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.alterjoc.radar.common.Constants.TAG_SERVICE;

public abstract class AbstractService extends Service
{

   static final String ACTION_FOREGROUND = "com.alterjoc.radar.client.FOREGROUND";
   static final String ACTION_BACKGROUND = "com.alterjoc.radar.client.BACKGROUND";
   public static final String ACTION_SYNC = "com.alterjoc.radar.client.SYNC";

   private static final Class[] mStartForegroundSignature = new Class[]{
      int.class, Notification.class};
   private static final Class[] mStopForegroundSignature = new Class[]{boolean.class};
   
   private NotificationManager mNM;
   private Method mStartForeground;
   private Method mStopForeground;
   private Object[] mStartForegroundArgs = new Object[2];
   private Object[] mStopForegroundArgs = new Object[1];

   protected NotificationManager getNotificationManager()
   {
      if (mNM == null)
      {
         mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      }
      return mNM;
   }

   @Override
   public void onCreate() {
      Log.i(TAG_SERVICE, "onCreate");
       try {
           mStartForeground = getClass().getMethod("startForeground",
                   mStartForegroundSignature);
           mStopForeground = getClass().getMethod("stopForeground",
                   mStopForegroundSignature);
       }
       catch (NoSuchMethodException e) {
           // Running on an older platform.
           mStartForeground = mStopForeground = null;
       }
   }

   // This is the old onStart method that will be called on the pre-2.0
   // platform. On 2.0 or later we override onStartCommand() so this
   // method will not be called.

   @Override
   public void onStart(Intent intent, int startId) {
      Log.i(TAG_SERVICE, "onStart : " + intent);
      handleCommand(intent);
   }

   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.i(TAG_SERVICE, "onStartCommand : " + intent);
      handleCommand(intent);
       // We want this service to continue running until it is explicitly
       // stopped, so return sticky.
       return ServiceConstants.START_STICKY;
   }

   void handleCommand(Intent intent) {
       System.out.println("AbstractService.handleCommand: " + intent);
       // fix:  tukaj se zgodi NPE (back na filemaangerju)
       if (intent == null) {
           doStart();
           return;
       }
       if (ACTION_FOREGROUND.equals(intent.getAction())) {
           // In this sample, we'll use the same text for the ticker and the
           // expanded notification
           String text = getResources().getString(R.string.application_name);

           // Set the icon, scrolling text and timestamp
           Notification notification = new Notification(R.drawable.tozibaba, text, System.currentTimeMillis());

           Intent notificationIntent = new Intent(this, EventsMapActivity.class);
           //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
           //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

           // The PendingIntent to launch our activity if the user selects this
           // notification
           PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

           // Set the info for the views that show in the notification panel.
           notification.setLatestEventInfo(this, "", text, contentIntent);

           startForegroundCompat(R.string.foreground_service_started, notification);
           doStart();
       } else if (ACTION_BACKGROUND.equals(intent.getAction())) {
           stopForegroundCompat(R.string.foreground_service_started);
       } else if (ACTION_SYNC.equals(intent.getAction())){
           System.out.println("AbstractService.handleCommand -------------------");
           // TODO :: this doStart call starts the service, but no icon is shown in the phone toolbar
           doStart();
           processSyncIntent();
       }
   }

    abstract void processSyncIntent();

   /**
    * This is a wrapper around the new startForeground method, using the older
    * APIs if it is not available.
    */
   void startForegroundCompat(int id, Notification notification) {
       // If we have the new startForeground API, then use it.
       if (mStartForeground != null) {
           mStartForegroundArgs[0] = Integer.valueOf(id);
           mStartForegroundArgs[1] = notification;
           try {
               mStartForeground.invoke(this, mStartForegroundArgs);
           }
           catch (InvocationTargetException e) {
               // Should not happen.
               Log.w(TAG_SERVICE, "Unable to invoke startForeground", e);
           }
           catch (IllegalAccessException e) {
               // Should not happen.
               Log.w(TAG_SERVICE, "Unable to invoke startForeground", e);
           }
           return;
       }

       // Fall back on the old API.
       setForeground(true);
       getNotificationManager().notify(id, notification);
   }

   /**
    * This is a wrapper around the new stopForeground method, using the older
    * APIs if it is not available.
    */
   void stopForegroundCompat(int id) {
       // If we have the new stopForeground API, then use it.
       if (mStopForeground != null) {
           mStopForegroundArgs[0] = Boolean.TRUE;
           try {
               mStopForeground.invoke(this, mStopForegroundArgs);
           }
           catch (InvocationTargetException e) {
               // Should not happen.
               Log.w(TAG_SERVICE, "Unable to invoke stopForeground", e);
           }
           catch (IllegalAccessException e) {
               // Should not happen.
               Log.w(TAG_SERVICE, "Unable to invoke stopForeground", e);
           }
           return;
       }

       // Fall back on the old API. Note to cancel BEFORE changing the
       // foreground state, since we could be killed at that point.
       getNotificationManager().cancel(id);
       setForeground(false);
   }

   @Override
   public void onDestroy() {
      Log.i(TAG_SERVICE, "onDestroy");
      doStop();
       // Make sure our notification is gone.
       stopForegroundCompat(R.string.foreground_service_started);
   }

   @Override
   public IBinder onBind(Intent intent) {
       return null;
   }


   protected abstract void doStart();

   protected abstract void doStop();
   
}
