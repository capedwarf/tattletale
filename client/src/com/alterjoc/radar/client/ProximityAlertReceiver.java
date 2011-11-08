package com.alterjoc.radar.client;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.database.DBAdapter;
import static com.alterjoc.radar.common.Constants.TAG_PROXIMITY_ALERT;

/**
 * User: Dejan
 * Date: 30.9.2010
 * Time: 16:54:42
 */
public class ProximityAlertReceiver extends BroadcastReceiver {
    private static int HELLO_ID = 1;

    // TODO increase the number on notification in status bar instead of having multiple notifications
    // The number would already have been implemented, but then only the last message is visible in drop down

    @Override
    public void onReceive(Context context, Intent intent) {
       try
       {
           if (intent.getAction() == null || !intent.getAction().startsWith(Tools.PROXIMITY_ACTION)) {
               return;
           }
   
           boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
           if (!isEntering) {
               return;
           }
           DBAdapter adapter = Application.getInstance().getDBHelper(context);
           // This is added for just in case check - so the user doesnt get the same notification twice
           // it should never happen, but since proximity alerts are giving me a headache, I added this anyway
           // For example: if I added a proximity alert which never expires, it NEVER expired
   
           NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
   
           String topic = intent.getStringExtra("topic");
           String title = intent.getStringExtra("title");
           String id = intent.getStringExtra("eventId");
           String tickerText = "Tožibaba sporočilo: " + topic;
           Log.i(TAG_PROXIMITY_ALERT, "PROXIMITY ALERT: " + topic + ", " + title + ", " + id);
           if (adapter.isEventUserNotified(id)) {
               Log.w(TAG_PROXIMITY_ALERT, "Old notification seems to be laying around. Event id = " + id + ". User will not be notified.");
               return;
           }
   
           Notification notification = new Notification(R.drawable.tozibaba, tickerText, System.currentTimeMillis());
           notification.flags |= Notification.FLAG_AUTO_CANCEL;
   
   
           SharedPreferences prefs = Tools.getAppPreferences(context);
           SharedPreferences.Editor editor = prefs.edit();
           editor.putLong("eventId", Long.parseLong(id));
           editor.commit();
   
           Intent notificationIntent = new Intent(context, EventsMapActivity.class);
           PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
   
           notification.setLatestEventInfo(context, topic, title, contentIntent);
           // TODO:: make the text of the event appear as it appears when selected:
           // TODO :: add to settings or dont add at all - it could be annoying for user
           // TODO :: dont forget to add permissons to xml
           
           Preferences masterPrefs = Application.getInstance().getPreferences(context);
           if (masterPrefs.isUsingSoundNotification())
              notification.defaults |= Notification.DEFAULT_SOUND;        
           if (masterPrefs.isUsingVibrateNotification())
              notification.defaults |= Notification.DEFAULT_VIBRATE;
   
           mNotificationManager.notify(HELLO_ID, notification);
           // This is used to have each notification seperate
           // mNotificationManager.notify(HELLO_ID++, notification);
           adapter.setEventNotified(Long.parseLong(id));
       }
       catch (Throwable th)
       {
          Log.e(TAG_PROXIMITY_ALERT, "Exception while handling proximity alert: ", th);
       }
    }
}
