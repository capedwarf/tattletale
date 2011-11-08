package com.alterjoc.radar.client;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.alterjoc.radar.client.adapters.TopicAdapter;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.client.log.Log;
import com.alterjoc.radar.client.login.UserWizardActivity;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.Constants;
import org.jboss.capedwarf.common.dto.Identity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.alterjoc.radar.common.Constants.TAG_GUI;
import static com.alterjoc.radar.common.Constants.TAG_TOOLS;

/**
 * User: Dejan
 * Date: 16.8.2010
 * Time: 11:09:00
 */
public class Tools {

    public static List<PendingIntent> proximityAlertIntents = new ArrayList<PendingIntent>();

    private static Location currentLocation;
    private static Criteria criteria;

    public static final int SMALL = 0;
    public static final int MEDIUM = 1;
    public static final int BIG = 2;
    public static final int HUGE = 3;

    private static final String DATE_FORMAT = "dd.MM HH:mm";
    public static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

    private static int displaySize = -1;

    public static final String PROXIMITY_ACTION = "PROXIMITY_ACTION";

    private static Map[] iconsMap = new Map[4];

    public static final Map<String, Long> timeMap = new LinkedHashMap<String, Long>();

    static {
        timeMap.put("1 ura", Constants.HOUR);
        timeMap.put("3 ure", Constants.THREE_HOURS);
        timeMap.put("12 ur", Constants.TWELVE_HOURS);
        timeMap.put("1 dan", Constants.DAY);
        timeMap.put("3 dni", Constants.THREE_DAYS);
        timeMap.put("1 teden", Constants.WEEK);
        timeMap.put("2 tedna", Constants.TWO_WEEK);
        timeMap.put("1 mesec", Constants.MONTH);
    }

    static {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("radar", R.drawable.ic_menu_radar36);
        map.put("event", R.drawable.ic_menu_event36);
        map.put("filter", R.drawable.ic_menu_filter36);
        map.put("preferences", R.drawable.ic_menu_preferences36);
        map.put("addTopic", R.drawable.ic_menu_add36);
        map.put("subscriptions", R.drawable.ic_menu_subscriptions36);
        map.put("exit", R.drawable.ic_menu_exit36);
        map.put("check", R.drawable.ic_menu_check36);
        map.put("delete", R.drawable.ic_menu_delete36);
        map.put("view", R.drawable.ic_menu_view36);
        map.put("menulist", R.drawable.ic_menu_list36);
        map.put("archive", R.drawable.ic_menu_archive36);
        map.put("info", R.drawable.ic_menu_info_details36);
        map.put("set_archived", R.drawable.ic_menu_set_archived36);
        map.put("map", R.drawable.map22);
        map.put("list", R.drawable.list22);
        map.put("ok", R.drawable.apply22);
        map.put("resetLocation", R.drawable.reset_location22);
        map.put("removephoto", R.drawable.removephoto32);
        map.put("refresh", R.drawable.refresh22);
        map.put("addphoto", R.drawable.addphoto32);
        map.put("switch", R.drawable.switch22);
        map.put("send", R.drawable.send);
        map.put("folderphoto", R.drawable.folder_photo22);
        map.put("filter_button", R.drawable.filter22);
        iconsMap[0] = map;
        map = new HashMap<String, Integer>();
        map.put("menulist", R.drawable.ic_menu_list48);
        map.put("radar", R.drawable.ic_menu_radar48);
        map.put("event", R.drawable.ic_menu_event48);
        map.put("filter", R.drawable.ic_menu_filter48);
        map.put("preferences", R.drawable.ic_menu_preferences48);
        map.put("addTopic", R.drawable.ic_menu_add48);
        map.put("subscriptions", R.drawable.ic_menu_subscriptions36);
        map.put("exit", R.drawable.ic_menu_exit48);
        map.put("check", R.drawable.ic_menu_check48);
        map.put("delete", R.drawable.ic_menu_delete48);
        map.put("view", R.drawable.ic_menu_view48);
        map.put("info", R.drawable.ic_menu_info_details48);
        map.put("set_archived", R.drawable.ic_menu_set_archived48);
        map.put("map", R.drawable.map32);
        map.put("list", R.drawable.list32);
        map.put("refresh", R.drawable.refresh32);
        map.put("switch", R.drawable.switch32);
        map.put("ok", R.drawable.apply32);
        map.put("resetLocation", R.drawable.reset_location32);
        map.put("removephoto", R.drawable.removephoto32);
        map.put("addphoto", R.drawable.addphoto32);
        map.put("send", R.drawable.send);
        map.put("folderphoto", R.drawable.folder_photo32);
        map.put("archive", R.drawable.ic_menu_archive48);
        map.put("filter_button", R.drawable.filter32);
        iconsMap[1] = map;

        map = new HashMap<String, Integer>();
        map.put("menulist", R.drawable.ic_menu_list48);
        map.put("radar", R.drawable.ic_menu_radar48);
        map.put("event", R.drawable.ic_menu_event48);
        map.put("filter", R.drawable.ic_menu_filter48);
        map.put("preferences", R.drawable.ic_menu_preferences48);
        map.put("addTopic", R.drawable.ic_menu_add48);
        map.put("subscriptions", R.drawable.ic_menu_subscriptions36);
        map.put("exit", R.drawable.ic_menu_exit48);
        map.put("check", R.drawable.ic_menu_check48);
        map.put("delete", R.drawable.ic_menu_delete48);
        map.put("view", R.drawable.ic_menu_view48);
        map.put("archive", R.drawable.ic_menu_archive48);
        map.put("info", R.drawable.ic_menu_info_details48);
        map.put("set_archived", R.drawable.ic_menu_set_archived48);
        map.put("map", R.drawable.map32);
        map.put("list", R.drawable.list32);
        map.put("refresh", R.drawable.refresh32);
        map.put("ok", R.drawable.apply32);
        map.put("switch", R.drawable.switch32);
        map.put("resetLocation", R.drawable.reset_location32);
        map.put("removephoto", R.drawable.removephoto64);
        map.put("addphoto", R.drawable.addphoto64);
        map.put("send", R.drawable.send64);
        map.put("folderphoto", R.drawable.folder_photo64);
        map.put("filter_button", R.drawable.filter32);
        iconsMap[2] = map;
        map = new HashMap<String, Integer>();
        map.put("menulist", R.drawable.ic_menu_list72);
        map.put("radar", R.drawable.ic_menu_radar72);
        map.put("event", R.drawable.ic_menu_event72);
        map.put("filter", R.drawable.ic_menu_filter72);
        map.put("subscriptions", R.drawable.ic_menu_subscriptions48);
        map.put("preferences", R.drawable.ic_menu_preferences72);
        map.put("addTopic", R.drawable.ic_menu_add72);
        map.put("exit", R.drawable.ic_menu_exit72);
        map.put("check", R.drawable.ic_menu_check72);
        map.put("delete", R.drawable.ic_menu_delete72);
        map.put("view", R.drawable.ic_menu_view72);
        map.put("archive", R.drawable.ic_menu_archive72);
        map.put("info", R.drawable.ic_menu_info_details72);
        map.put("set_archived", R.drawable.ic_menu_set_archived72);
        map.put("map", R.drawable.map32);
        map.put("list", R.drawable.list32);
        map.put("switch", R.drawable.switch32);
        map.put("refresh", R.drawable.refresh32);
        map.put("ok", R.drawable.apply32);
        map.put("resetLocation", R.drawable.reset_location32);
        map.put("removephoto", R.drawable.removephoto64);
        map.put("addphoto", R.drawable.addphoto64);
        map.put("send", R.drawable.send64);
        map.put("folderphoto", R.drawable.folder_photo64);
        map.put("filter_button", R.drawable.filter32);
        iconsMap[3] = map;
    }

    @SuppressWarnings({"unchecked"})
    private static void sort(List list, Comparator comparator, boolean reverse) {
        if (list == null || list.isEmpty() || list.size() == 1)
            return;

        if (reverse)
            comparator = Collections.reverseOrder(comparator);

        Collections.sort(list, comparator);
    }

    public static void reorderTopics(List<TopicInfo> topics) {
        sort(topics, new TopicComparator(), false);
    }

    public static void reorderCommentsByTimestamp(List<CommentInfo> comments) {
        sort(comments, new CommentComparator(), true);
    }

    public static void reorderEventsByTimestamp(List<EventInfo> events) {
        sort(events, new EventTimeComparator(), true);
    }

    public static String getAddress(double latitude, double longitude, Context context) {
        Geocoder geocoder = new Geocoder(context);
        String result = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String street = address.getAddressLine(0);
                String city = address.getLocality();
                String country = address.getCountryName();
                result = street + ", " + city + ", " + country;
                result = result.replace("null", "-");
            }
        } catch (IOException e) {
            Log.e(TAG_TOOLS, "getFromLocation service not available");
            e.printStackTrace();
            return "-";
        }
        return result == null ? "-" : result;
    }

    public static String getLocationProvider(Context context) {
        if (criteria == null) {
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
        }
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.getBestProvider(criteria, true);
    }

    public static Location getDefaultLocation() {
        Location location = new Location("na");
        location.setLongitude(14.493157);
        location.setLatitude(46.030632);
        return location;
    }

    /**
     * The method never returns null - if location cant be defined the default is returned - LJ.
     *
     * @param context
     * @return
     */
    public static Location getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = getLocationProvider(context);
        if (provider == null) {
            Log.w(TAG_TOOLS, "No provider defined for location.");
        } else {
            try {
                currentLocation = locationManager.getLastKnownLocation(provider);
            } catch (Exception e) {
                // ignore...
            }
        }
        if (currentLocation == null) {
            Log.w(TAG_TOOLS, "Current location is not defined. provider = " + provider);
            currentLocation = getDefaultLocation();
        }
        return currentLocation;
    }

    public static double getDistanceFromCurrentLocation(Context context, float latitude, float longitude) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (criteria == null) {
            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
        }
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider == null && currentLocation == null) {
            return -1;
        }

        if (provider != null) {
            currentLocation = locationManager.getLastKnownLocation(provider);
        }


        float[] result = new float[]{0F};
        try {
            if (currentLocation != null) {
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), latitude / 1000000d, longitude / 1000000d, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return result[0];
    }

    public static int getImage(Context context, String imageName) {
        Map map = iconsMap[getDisplaySize(context)];
        return (Integer) map.get(imageName);
    }

    public static void buildMainMenu(Menu menu, Context context, boolean addEventListMenu, boolean addRadarMenu, boolean addInfoMenu, boolean addFilterMenu) {
        Map map = iconsMap[getDisplaySize(context)];

        int position = 1;
        MenuItem item;
        if (addEventListMenu) {
            item = menu.add(1, 100, position++, R.string.list);
            item.setIcon((Integer) map.get("menulist"));
        }
        if (addRadarMenu) {
            item = menu.add(1, 101, position++, "Radar");
            item.setIcon((Integer) map.get("radar"));
        }
        if (addFilterMenu) {
            item = menu.add(1, 103, position++, R.string.filter);
            item.setIcon((Integer) map.get("filter"));
        }


        item = menu.add(1, 200, position++, R.string.publish_event);
        item.setIcon((Integer) map.get("event"));

        item = menu.add(1, 300, position++, R.string.subscriptions);
        item.setIcon((Integer) map.get("subscriptions"));

        item = menu.add(1, 400, position++, R.string.add_channel);
        item.setIcon((Integer) map.get("addTopic"));

        item = menu.add(1, 500, position++, R.string.settings);
        item.setIcon((Integer) map.get("preferences"));

        item = menu.add(1, 600, position++, R.string.exit);
        item.setIcon((Integer) map.get("exit"));

        if (addInfoMenu) {
            item = menu.add(1, 102, position, "Info");
            item.setIcon((Integer) map.get("info"));
        }
    }

    public static boolean processOptionsItemSelected(MenuItem item, Activity activity) {
        Intent intent;

        switch (item.getItemId()) {
            case 100:
                intent = new Intent(activity, EventListActivity.class);
                activity.startActivityForResult(intent, 0);
                return true;
            case 102:
                intent = new Intent(activity, AboutActivity.class);
                activity.startActivityForResult(intent, 0);
                return true;
            case 103:
                // show dialog
                return true;
            case 101:
                intent = new Intent(activity, EventsMapActivity.class);
                activity.startActivityForResult(intent, 0);
                return true;
            case 200:
                intent = new Intent(activity, PostEventActivity.class);
                activity.startActivityForResult(intent, 0);
                return true;
            case 300:
                intent = new Intent(activity, EditSubscriptionsActivity.class);
                activity.startActivityForResult(intent, 0);
                return true;
            case 400:
                intent = new Intent(activity, AddTopicActivity.class);
                activity.startActivityForResult(intent, 0);
                return true;
            case 500:
                intent = new Intent(activity, SettingsActivity.class);
                activity.startActivityForResult(intent, 0);
                return true;
            case 600:
                activity.stopService(new Intent(activity, TozibabaService.class));
                activity.setResult(Application.RESULT_EXIT);
                activity.finish();
                return true;
            default:
                return false;
        }
    }

    public static int getButtonsFontSize(Context context) {
        switch (getDisplaySize(context)) {
            case SMALL:
                return 12;
            case MEDIUM:
                return 14;
            case BIG:
                return 16;
        }
        return 12;
    }

    public static int getDisplaySize(Context context) {
        if (displaySize == -1) {

            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            int size = display.getWidth() * display.getHeight();
            if (size <= 320 * 240) {
                displaySize = SMALL;
            } else if (size < 320 * 500) {
                displaySize = MEDIUM;
            } else if (size < 400 * 800) {
                displaySize = BIG;
            } else {
                displaySize = HUGE;
            }
        }
        return displaySize;
    }

    private static class EventDistanceComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((EventInfo) o1).getDistance().compareTo(((EventInfo) o2).getDistance());
        }
    }

    private static class EventTimeComparator implements Comparator<EventInfo> {
        public int compare(EventInfo o1, EventInfo o2) {
            return (new Long(o1.getTimestamp()).compareTo(new Long(o2.getTimestamp())));
        }
    }

    private static class CommentComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return (new Long(((CommentInfo) o1).getTimestamp()).compareTo(((CommentInfo) o2).getTimestamp()));
        }
    }

    private static class TopicComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            // The radar topic is on top
            if (((TopicInfo) o1).getId() == 1) {
                return -1;
            }
            if (((TopicInfo) o2).getId() == 1) {
                return 1;
            }

            // Subscribed topics are second
            if (((TopicInfo) o1).isUserSubscribed() && !((TopicInfo) o1).isUserSubscribed()) {
                return -1;
            }
            if (!((TopicInfo) o1).isUserSubscribed() && ((TopicInfo) o1).isUserSubscribed()) {
                return 1;
            }
            // All other topics are third
            return ((TopicInfo) o1).getName().compareTo(((TopicInfo) o2).getName());
        }
    }

    public static DialogInterface.OnClickListener wrapProtect(DialogInterface.OnClickListener listener) {
        return new DialogOnClickWrapper(listener);
    }

    public static Handler wrapProtect(Handler handler) {
        return new HandlerWrapper(handler);
    }

    public static View.OnClickListener wrapProtect(View.OnClickListener listener) {
        return new ViewOnClickWrapper(listener);
    }

    static class ViewOnClickWrapper implements View.OnClickListener {
        private View.OnClickListener delegate;

        private ViewOnClickWrapper(View.OnClickListener delegate) {
            this.delegate = delegate;
        }

        public void onClick(View v) {
            try {
                delegate.onClick(v);
            }
            catch (Exception ex) {
                Log.e(TAG_GUI, "onClick() failed: " + delegate, ex);
            }
        }
    }

    static class DialogOnClickWrapper implements DialogInterface.OnClickListener {
        private DialogInterface.OnClickListener delegate;

        private DialogOnClickWrapper(DialogInterface.OnClickListener delegate) {
            this.delegate = delegate;
        }

        public void onClick(DialogInterface dialog, int which) {
            try {
                delegate.onClick(dialog, which);
            }
            catch (Exception ex) {
                Log.e(TAG_GUI, "onClick() failed: " + delegate, ex);
            }
        }
    }

    static class HandlerWrapper extends Handler {
        private Handler delegate;

        HandlerWrapper(Handler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                delegate.handleMessage(msg);
            }
            catch (Exception ex) {
                Log.e(TAG_GUI, "handleMessage() failed: " + delegate, ex);
            }
        }

    }

    public static boolean isUserLoggedIn(Activity activity, boolean redirectToNewUserActivity) {
        String login = Application.getInstance().getPreferences(activity).getUserLogin();
        if (login == null || login.trim().length() == 0) {
            if (redirectToNewUserActivity) {
                Intent intent = new Intent(activity, UserWizardActivity.class);
                activity.startActivityForResult(intent, 0);
            }
            Toast.makeText(activity, R.string.login_required, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public static SharedPreferences getAppPreferences(Context ctx) {
        return ctx.getSharedPreferences("app-prefs", Context.MODE_PRIVATE);
    }

    public static int findItemInList(Identity item, List<? extends Identity> list) {
        if (item == null)
            return -1;

        int i = 0;
        for (Identity cur : list) {
            if (item.getId().equals(cur.getId())) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public static void showFilterDialog(final Context context, final AfterFilterChangeAction action) {

        final ListView listView = new ListView(context);
        DBAdapter dbAdapter = Application.getInstance().getDBHelper(context);
        List<TopicInfo> topics = dbAdapter.getSubscribedTopics();
        Tools.reorderTopics(topics);
        ListAdapter adapter = new TopicAdapter(context, android.R.layout.simple_list_item_multiple_choice, topics, false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);
        for (int i = 0; i < listView.getAdapter().getCount(); i++) {
            listView.setItemChecked(i, !((TopicInfo) listView.getItemAtPosition(i)).isFilterApplied());
        }

        final Dialog dialog = new Dialog(context);

        ImageButton okButton = new ImageButton(context);
        okButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ok));
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DBAdapter dbAdapter = Application.getInstance().getDBHelper(context);
                List<TopicInfo> topics = new ArrayList<TopicInfo>();
                for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                    TopicInfo topic = (TopicInfo) listView.getItemAtPosition(i);
                    topic.setFilterApplied(!listView.isItemChecked(i));
                    topics.add(topic);
                }
                dbAdapter.saveEntities(topics);
                dialog.dismiss();
                action.doAction();
            }
        });
        ImageButton cancelButton = new ImageButton(context);
        cancelButton.setImageDrawable(context.getResources().getDrawable(R.drawable.cancel));
        cancelButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        LinearLayout dialogView = new LinearLayout(context);
        dialogView.setOrientation(LinearLayout.VERTICAL);
        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.addView(okButton);
        buttons.addView(cancelButton);
        dialogView.addView(buttons);
        dialogView.addView(listView);

        dialog.setTitle(R.string.event_filter);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        dialog.show();

    }

    public static interface AfterFilterChangeAction{
        void doAction();
    }
}
