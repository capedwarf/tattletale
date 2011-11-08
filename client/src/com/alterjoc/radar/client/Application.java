package com.alterjoc.radar.client;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.alterjoc.radar.client.database.AbstractDBAdapter;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.database.sqlite.SQLiteDBAdapter;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.log.LogLevel;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import com.google.android.maps.GeoPoint;
import org.jboss.capedwarf.connect.config.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Application {

    public static final int RESULT_EXIT = 0x00F00000;

    public static final DialogInterface.OnClickListener NULL_CLICK_LISTENER = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog,
                            int whichButton) {
        }
    };

    private static final String EXTRA_PROPERTIES_FILE = "extra.properties";
    private static Application instance;
    private static Properties props;
    
    private Preferences prefs;
    private MasterLocationListener masterLocListener = new MasterLocationListener();

    private AbstractDBAdapter dbAdapter;
    private ServerTime serverTime;

    private int proximityAlertRadius;
    private EventsMapActivity mainActivity;
    private EventDetailsActivity eventActivity;

    public static Application getInstance() {
        if (instance == null) {
            instance = new Application();
        }
        return instance;
    }

    public void init(Context context) {
        loadExtraProperties();
        initSharedPrefs(context);
        initLoggingLevels();
        
        // Just call this to start the service
        TozibabaService.getInstance(context);
        /*
        Configuration.setInstance(new com.alterjoc.radar.connect.config.DefaultConfiguration() {
            {
               setDebugMode(true);
            }           
        });
        */
        // register DBAdapter event listener
        getDBHelper(context).addListener(new DBAdapter.Listener() {
         
         public void topicsUpdated(List<TopicInfo> topics)
         {}
         
         public void eventsUpdated(List<EventInfo> events)
         {
            EventsMapActivity copy = mainActivity;
            if (copy != null && events.size() > 0)
            {
               copy.addEventsToMap(events);
            }
         }
         
         public void commentsUpdated(List<CommentInfo> comments)
         {
            EventDetailsActivity copy = eventActivity;
            if (copy != null && comments.size() > 0)
            {
               copy.addComments(comments);
            }
         }
      });
    }

    private void initLoggingLevels()
    {
       Log.setDefaultLevel(Configuration.getInstance().isDebugLogging() ? LogLevel.DEBUG : LogLevel.WARN);
    }
    
   private void initSharedPrefs(Context context) {
        prefs = new Preferences(PreferenceManager.getDefaultSharedPreferences(context), context.getResources());
    }

    public synchronized DBAdapter getDBHelper(Context context) {
        if (dbAdapter == null) {
            dbAdapter = new SQLiteDBAdapter(context);
        }
        return dbAdapter;
    }

    protected synchronized void closeDBHelper() {
        if (dbAdapter != null) {
            dbAdapter.close();
            dbAdapter = null;
        }
    }

    public static Properties getProperties() {
        if (props == null) {
            loadExtraProperties();
        }
        return props;
    }

    public Preferences getPreferences(Context context) {
        // TODO :: there is a broken concept here::
        // prefs become null, and they need context to be created
        // but there are classes using this method which are interfaces implemented on GAE!

        if (prefs == null) {
            initSharedPrefs(context);
        }
        return prefs;
    }

    private static void loadExtraProperties() {
        if (props == null) {
            props = new Properties();
        }
        InputStream in = Application.class.getResourceAsStream(EXTRA_PROPERTIES_FILE);
        if (in == null)
            throw new RuntimeException(EXTRA_PROPERTIES_FILE + " missing!");

        try {
            props.load(in);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load " + EXTRA_PROPERTIES_FILE, e);
        }
    }

    public MasterLocationListener getMasterLocationListener() {
        return masterLocListener;
    }

    public class MasterLocationListener implements LocationListener {
        private HashMap<Long, ProximityAlert> proximityAlerts = new HashMap<Long, ProximityAlert>();
        private LinkedList<LocationListener> listeners = new LinkedList<LocationListener>();

        @SuppressWarnings("unchecked")
        public synchronized void addListener(LocationListener lis) {
            for (LocationListener l : listeners) {
                if (l == lis)
                    return;
            }
            LinkedList<LocationListener> nu = (LinkedList<LocationListener>) listeners.clone();
            nu.add(lis);
            listeners = nu;
        }

        public synchronized LocationListener removeListener(LocationListener lis) {
            LinkedList<LocationListener> nu = new LinkedList<LocationListener>();
            LocationListener ret = null;

            for (LocationListener l : listeners) {
                if (l != lis) {
                    nu.add(lis);
                } else {
                    ret = l;
                }
            }
            listeners = nu;
            return ret;
        }

        public synchronized void clearAllListeners() {
            listeners = new LinkedList<LocationListener>();
        }

        public synchronized void clearProximityAlerts(Context context) {
            // I added null check because of Force Close error due to null pointer error in some cases
            if (context != null) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                for (ProximityAlert proximityAlert : proximityAlerts.values()) {
                    locationManager.removeProximityAlert(proximityAlert.getIntent());
                }
            }
            proximityAlerts = new HashMap<Long, ProximityAlert>();
        }

        
        public synchronized void addProximityAlert(Context context, EventInfo event)
        {
           if (!event.isUserNotified() && !event.isArchived())
           {
              // if there's alert for this event already, we don't add it
              ProximityAlert pa = proximityAlerts.get(event.getId());
              if (pa == null)
              {
                 pa = createProximityAlert(context, event);
                 LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                 locationManager.addProximityAlert(pa.getLatitude(), pa.getLongitude(), pa.getRadius(), -1, pa.getIntent());
                 proximityAlerts.put(event.getId(), pa);
              }
           }
        }
        
        public synchronized void addProximityAlerts(Context context, List<EventInfo> events)
        {
           for (EventInfo event: events)
           {
              addProximityAlert(context, event);
           }
        }
        
        /**
         * This method is called if radius setting changes.
         * ProximityAlerts contain radius, so the old ones have to be all removed if radius changed. 
         * This method also removes any registered alerts whose events' archive flag has changed.
         * 
         * @param context the context
         */
        @SuppressWarnings("unchecked")
        public synchronized void resetProximityAlerts(Context context)
        {
            DBAdapter adapter = Application.getInstance().getDBHelper(context);
            List<EventInfo> events = adapter.getEvents(false);

            int newRadius = Integer.parseInt(Application.getInstance().getPreferences(context).getEffectiveRadiusValue());
            boolean replaceAll = false;

            // If radius has changed remove all alerts and add new ones.
            if (newRadius != proximityAlertRadius)
            {
                replaceAll = true;
            }
            
            proximityAlertRadius = newRadius;

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (replaceAll)
            {
               for (ProximityAlert pa: proximityAlerts.values())
               {
                  locationManager.removeProximityAlert(pa.getIntent());
               }
               proximityAlerts.clear();
            }
            
            HashMap<Long, ProximityAlert> alerts = new HashMap<Long, ProximityAlert>();
            for (EventInfo event : events)
            {   
                //System.out.println("EventInfo.smallPhotoBytes.len: " + event.getSmallPhoto() != null ? event.getSmallPhoto().length : null);
                //System.out.println("EventInfo.bigPhotoBytes.len: " + event.getPhoto() != null ? event.getPhoto().length : null);

                if (!event.isUserNotified() && !event.isArchived())
                {
                    ProximityAlert alert = createProximityAlert(context, event);
                    alerts.put(alert.getEventId(), alert);
                }
            }
                        
            // iterate over existing and check if they exist in alerts
            // alerts represents the exact registered set
            if (!replaceAll)
            {
               Iterator<Map.Entry<Long, ProximityAlert>> it = proximityAlerts.entrySet().iterator();
               while (it.hasNext())
               {
                  Map.Entry<Long, ProximityAlert> ent = it.next();
                  ProximityAlert pa = ent.getValue();
                  if (alerts.containsKey(pa.getEventId()) == false)
                  {
                     locationManager.removeProximityAlert(pa.getIntent());
                     it.remove();
                  }
               }
            }
            
            for (ProximityAlert pa : alerts.values())
            {
                if (!replaceAll)
                {
                   ProximityAlert prev = proximityAlerts.get(pa.getEventId());
                   if (prev != null)
                   {
                      // skip adding - it already registered
                      continue;
                   }                   
                }
                
                locationManager.addProximityAlert(pa.getLatitude(), pa.getLongitude(), pa.getRadius(), -1, pa.getIntent());
                proximityAlerts.put(pa.getEventId(), pa);
            }
        }

      private ProximityAlert createProximityAlert(Context context,
            EventInfo event)
      {
         GeoPoint point = new GeoPoint(event.getLatitude(), event.getLongitude());
           // Create unique intent in order to get unique pendingIntent:
           Intent intent = new Intent(context, ProximityAlertReceiver.class);
           intent.setAction(Tools.PROXIMITY_ACTION + event.getId());
           Bundle bundle = new Bundle();
           bundle.putString("eventId", "" + event.getId());
           bundle.putString("title", event.getTitle());
           bundle.putString("topic", event.getTopicInfo().getName());
           intent.putExtras(bundle);
           PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
           int radius = Integer.parseInt(Application.getInstance().getPreferences(context).getEventNotificationRadius());
           return new ProximityAlert(event.getId(), pendingIntent, (double) point.getLatitudeE6() / 1000000d, (double) point.getLongitudeE6() / 1000000d, radius);
      }

        public void onLocationChanged(Location location) {
            for (LocationListener l : listeners) {
                l.onLocationChanged(location);
            }
        }

        public void onProviderDisabled(String provider) {
            for (LocationListener l : listeners) {
                l.onProviderDisabled(provider);
            }
        }

        public void onProviderEnabled(String provider) {
            for (LocationListener l : listeners) {
                l.onProviderEnabled(provider);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            for (LocationListener l : listeners) {
                l.onStatusChanged(provider, status, extras);
            }
        }
    }

    public static String getMapsKey() {
        String key = getProperties().getProperty("google.maps.key");
        if (key == null) {
            throw new RuntimeException("google.maps.key not found in extra properties");
        }
        return key;
    }

    public void initServerTime(long remoteTs) {
        serverTime = new ServerTime(remoteTs);
    }

    public ServerTime getServerTime() {
        return serverTime;
    }

    public static class ServerTime {
        private long remoteTs;
        private long localTs;
        private long timeDiff;

        ServerTime(long remoteTs) {
            localTs = System.currentTimeMillis();
            this.remoteTs = remoteTs;
            timeDiff = remoteTs - localTs;
        }

        public long getTimeDiff() {
            return timeDiff;
        }

        public long localToRemote(long ts) {
            return ts + timeDiff;
        }

        public long remoteToLocal(long ts) {
            return ts - timeDiff;
        }
    }

    public static boolean isDebug() {
        return true;  // TODO: set this to false before production
    }

   public void setMapActivity(EventsMapActivity mainActivity)
   {
      this.mainActivity = mainActivity;
   }

   public void setEventActivity(EventDetailsActivity activity)
   {
      this.eventActivity = activity; 
   }
}
