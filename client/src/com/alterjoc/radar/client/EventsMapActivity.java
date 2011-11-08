package com.alterjoc.radar.client;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.*;
import android.widget.*;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.overlays.EventOverlayItem;
import com.alterjoc.radar.client.overlays.EventSubscriptionsOverlay;
import com.alterjoc.radar.client.overlays.EventsOverlay;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.ImageInfo;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import java.util.*;

import static com.alterjoc.radar.common.Constants.TAG_MAIN;

/**
 * User: Dejan
 * Date: 14.8.2010
 * Time: 15:44:53
 */
public class EventsMapActivity extends MapActivity implements GestureDetector.OnGestureListener {

    private GestureDetector gestureDetector;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private EventsOverlay eventsOverlay;
    private EventSubscriptionsOverlay subscriptionsOverlay;
    private MapView mapView;
    private int zoom = 10;
    private MapLocationListener locListener;

    private boolean isSatelliteViewOn;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application app = Application.getInstance();
        app.init(this);
        boolean firstRun = app.getPreferences(this).isFirstRun();
        if (firstRun) {
            createAlert().show();

        }

        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        gestureDetector = new GestureDetector(this);

        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);

        String key = app.getMapsKey();

        mapView = new MapView(this, key);
        setContentView(mapView);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);

        ImageButton listViewButton = new ImageButton(this);
        listViewButton.setImageDrawable(this.getResources().getDrawable(Tools.getImage(this, "list")));
        listViewButton.setOnClickListener(Tools.wrapProtect(new View.OnClickListener() {
            public void onClick(View view) {
                eventsOverlay.resetViews();
                Intent intent = new Intent(EventsMapActivity.this, EventListActivity.class);
                EventsMapActivity.this.startActivity(intent);
            }
        }));
        ImageButton refreshEventsButton = new ImageButton(this);
        refreshEventsButton.setImageDrawable(this.getResources().getDrawable(Tools.getImage(this, "refresh")));
        refreshEventsButton.setOnClickListener(Tools.wrapProtect(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(EventsMapActivity.this, TozibabaService.class);
                intent.setAction(AbstractService.ACTION_SYNC);
                EventsMapActivity.this.startService(intent);
            }
        }));
        ImageButton filterButton = new ImageButton(this);
        filterButton.setImageDrawable(this.getResources().getDrawable(Tools.getImage(this, "filter_button")));
        filterButton.setOnClickListener(Tools.wrapProtect(new View.OnClickListener() {
            public void onClick(View view) {
                Tools.AfterFilterChangeAction action = new Tools.AfterFilterChangeAction() {
                    public void doAction() {
                        mapView.getOverlays().remove(eventsOverlay);
                        eventsOverlay = createEventsOverlay(mapView);
                        mapView.getOverlays().add(eventsOverlay);
                    }
                };
                Tools.showFilterDialog(EventsMapActivity.this, action);
            }
        }));

        ImageButton changeViewButton = new ImageButton(this);
        changeViewButton.setImageDrawable(this.getResources().getDrawable(Tools.getImage(this, "switch")));
        changeViewButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                isSatelliteViewOn = !isSatelliteViewOn;
                mapView.setSatellite(isSatelliteViewOn);
            }
        });

        LinearLayout topWrapperLayout = new LinearLayout(this);
        topWrapperLayout.setOrientation(LinearLayout.HORIZONTAL);
        topWrapperLayout.setGravity(Gravity.RIGHT);
        topWrapperLayout.addView(filterButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topWrapperLayout.addView(listViewButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topWrapperLayout.addView(refreshEventsButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topWrapperLayout.addView(changeViewButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        String provider = Tools.getLocationProvider(this);
        if (provider == null || !provider.equals(LocationManager.GPS_PROVIDER)) {
            Button enableLocationProvider = new Button(this);
            if (provider == null) {
                enableLocationProvider.setText(R.string.enable_locations);
            } else {
                enableLocationProvider.setText(R.string.enable_gps);
            }
            enableLocationProvider.setOnClickListener(Tools.wrapProtect(new View.OnClickListener() {
                public void onClick(View view) {
                    EventsMapActivity.this.finish();
                    Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    startActivity(myIntent);
                }
            }));
            topWrapperLayout.addView(enableLocationProvider, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        mapView.addView(topWrapperLayout, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Application.getInstance().setMapActivity(null);
        zoom = mapView.getZoomLevel();
        SharedPreferences settings = Tools.getAppPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("zoom", zoom);
        editor.commit();
        Application.getInstance().getMasterLocationListener().removeListener(locListener);
        Log.d(TAG_MAIN, "EventsMapActivity.onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Application.getInstance().setMapActivity(this);

        // Create the eventsOverlay for current location and radius:
        Location location = Tools.getCurrentLocation(this);
        if (location != null) {
            GeoPoint currentLocation = new GeoPoint((int) (location.getLatitude() * 1000000d), (int) (location.getLongitude() * 1000000d));
            mapView.getController().setCenter(currentLocation);
            if (subscriptionsOverlay != null) {
                mapView.getOverlays().remove(subscriptionsOverlay);
            }
            subscriptionsOverlay = new EventSubscriptionsOverlay();
            subscriptionsOverlay.setUsersLocation(currentLocation);
            mapView.getOverlays().add(subscriptionsOverlay);
        }

        mapView.getOverlays().remove(eventsOverlay);
        eventsOverlay = createEventsOverlay(mapView);
        mapView.getOverlays().add(eventsOverlay);

        SharedPreferences settings = Tools.getAppPreferences(this);
        zoom = settings.getInt("zoom", zoom);
        mapView.getController().setZoom(zoom);
        locListener = new MapLocationListener();
        Application.getInstance().getMasterLocationListener().addListener(locListener);

        long eventId = settings.getLong("eventId", -1);

        if (eventId != -1) {
            eventsOverlay.simulateTap(eventId);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("eventId", -1);
            editor.commit();
        }
    }

    private EventsOverlay createEventsOverlay(MapView mapView) {
        Drawable selectedMarker = this.getResources().getDrawable(R.drawable.event_bubble_10);
        selectedMarker.setBounds(0, 0, selectedMarker.getIntrinsicWidth(), selectedMarker.getIntrinsicHeight());
        EventsOverlay overlay = new EventsOverlay(this, mapView, selectedMarker);

        DBAdapter adapter = Application.getInstance().getDBHelper(this);
        List<EventInfo> events = adapter.getNonFilteredEvents(false);
        overlay.addOverlayItems(events);
        return overlay;
    }

    public static String trimString(String text) {
        if (text == null) {
            return "";
        }
        String result = text.substring(0, Math.min(23, text.length()));
        if (text.length() > result.length()) {
            result = result.substring(0, 20) + "...";
        }
        return result;
    }
    /// Implementation of gesturelistener

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("event = " + event);
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Tools.buildMainMenu(menu, this, false, false, true, false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return Tools.processOptionsItemSelected(item, this);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    public void onShowPress(MotionEvent motionEvent) {
    }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    public void onLongPress(MotionEvent motionEvent) {

    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float v1) {
        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            // right to left swipe
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                //flipper.setInAnimation(Tools.inFromLeftAnimation());
                //flipper.setOutAnimation(Tools.outToRightAnimation());
                //flipper.showPrevious();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    // GPS Listener Class

    private class MapLocationListener implements LocationListener {

        public void onLocationChanged(final Location location) {
            runOnUiThread(new Runnable() {

                public void run() {
                    GeoPoint currentLocation = new GeoPoint((int) (location.getLatitude() * 1000000d), (int) (location.getLongitude() * 1000000d));
                    mapView.getController().animateTo(currentLocation);
                    mapView.getOverlays().remove(subscriptionsOverlay);
                    subscriptionsOverlay = new EventSubscriptionsOverlay();
                    subscriptionsOverlay.setUsersLocation(currentLocation);
                    mapView.getOverlays().add(subscriptionsOverlay);
                    eventsOverlay.resetViews();
                }
            });
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status == 0)// UnAvailable
            {

            } else if (status == 1)// Trying to Connect
            {

            } else if (status == 2) {// Available

            }
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Application.RESULT_EXIT) {
            setResult(Application.RESULT_EXIT);
            finish();
        }
    }

    private Dialog createAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.hello);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView tv = new TextView(EventsMapActivity.this);
        tv.setGravity(Gravity.CENTER);

        tv.setText(Html.fromHtml(getResources().getString(R.string.welcome_1)), TextView.BufferType.SPANNABLE);
        linearLayout.addView(tv);

        tv = new TextView(EventsMapActivity.this);
        tv.setTextSize(12);
        tv.setGravity(Gravity.LEFT);
        tv.setPadding(5, 5, 5, 5);

        tv.setText(Html.fromHtml(getResources().getString(R.string.welcome)), TextView.BufferType.SPANNABLE);
        linearLayout.addView(tv);

        tv = new TextView(EventsMapActivity.this);
        tv.setTextSize(12);
        tv.setGravity(Gravity.LEFT);
        tv.setPadding(5, 5, 5, 5);
        tv.setText(Html.fromHtml(getResources().getString(R.string.quick_intro)), TextView.BufferType.SPANNABLE);
        linearLayout.addView(tv);

        ScrollView sc = new ScrollView(EventsMapActivity.this);
        sc.addView(linearLayout);
        builder.setView(sc);
        builder.setPositiveButton(getResources().getString(R.string.ok ), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Application.getInstance().getPreferences(EventsMapActivity.this).setHasRun();
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void addEventsToMap(final List<EventInfo> events) {

        if (events == null || events.size() == 0){
            return;
        }

        runOnUiThread(new Runnable() {
            public void run() {
                EventsOverlay copy = eventsOverlay;
                if (copy != null) {
                    List<EventInfo> relevantItems = new LinkedList<EventInfo>();
                    for (EventInfo event : events) {
                        if (!event.getTopicInfo().isFilterApplied()){
                            relevantItems.add(event);
                        }
                    }
                    copy.addOverlayItems(relevantItems);
                    eventsOverlay.resetViews();
                }
            }
        });
    }
}
