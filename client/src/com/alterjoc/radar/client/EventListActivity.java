package com.alterjoc.radar.client;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import com.alterjoc.radar.client.adapters.EventAdapter;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.common.data.EventInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Dejan
 * Date: 28.8.2010
 * Time: 9:08:10
 */
public class EventListActivity extends ListActivity {

    private EventAdapter eventListAdapter;
    private List<EventInfo> events;
    private boolean allSelected;
    private Handler handler;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        handler = new Handler();
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(1, 1, 1, "Radar");
        menuItem.setIcon(Tools.getImage(this, "radar"));
        Intent intent = new Intent(EventListActivity.this, EventsMapActivity.class);
        menuItem.setIntent(intent);

        menuItem = menu.add(1, 2, 2, R.string.select_all);
        menuItem.setIcon(Tools.getImage(this, "check"));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                eventListAdapter.checkAll(!allSelected);
                eventListAdapter.notifyDataSetChanged();
                allSelected = !allSelected;
                return true;
            }
        });

        menuItem = menu.add(1, 3, 3, R.string.put_to_archive);
        menuItem.setIcon(Tools.getImage(this, "set_archived"));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                List<EventInfo> checkedEvents = eventListAdapter.getCheckedEvents();
                for (EventInfo checkedEvent : checkedEvents) {
                    checkedEvent.setArchived(true);
                }
                DBAdapter adapter = Application.getInstance().getDBHelper(EventListActivity.this);
                adapter.saveEntities(checkedEvents);

                for (EventInfo checkedEvent : checkedEvents) {
                    eventListAdapter.remove(checkedEvent);
                }
                eventListAdapter.checkAll(false);
                eventListAdapter.notifyDataSetChanged();
                return true;
            }
        });

        menuItem = menu.add(1, 4, 4, R.string.delete);
        menuItem.setIcon(Tools.getImage(this, "delete"));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                List<EventInfo> checkedEvents = eventListAdapter.getCheckedEvents();
                for (EventInfo checkedEvent : checkedEvents) {
                    eventListAdapter.remove(checkedEvent);
                }
                DBAdapter adapter = Application.getInstance().getDBHelper(EventListActivity.this);
                adapter.deleteEntities(checkedEvents);
                eventListAdapter.checkAll(false);
                eventListAdapter.notifyDataSetChanged();

                return true;
            }
        });

        intent = new Intent(EventListActivity.this, EventArchiveListActivity.class);
        menuItem = menu.add(1, 5, 5, R.string.archive);
        menuItem.setIcon(Tools.getImage(this, "archive"));
        menuItem.setIntent(intent);

        menuItem = menu.add(1, 6, 6, R.string.filter);
        menuItem.setIcon(Tools.getImage(this, "filter"));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                Tools.AfterFilterChangeAction action = new Tools.AfterFilterChangeAction() {
                    public void doAction() {
                        DBAdapter dbAdapter = Application.getInstance().getDBHelper(EventListActivity.this);
                        events = dbAdapter.getNonFilteredEvents(false);
                        Tools.reorderEventsByTimestamp(events);

                        Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                        eventListAdapter = new EventAdapter(EventListActivity.this, android.R.layout.simple_list_item_multiple_choice, events);
                        eventListAdapter.setOnTextClickIntent(intent);
                        getListView().setAdapter(eventListAdapter);
                    }
                };

                Tools.showFilterDialog(EventListActivity.this, action);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DBAdapter dbAdapter = Application.getInstance().getDBHelper(this);
        events = dbAdapter.getNonFilteredEvents(false);
        Tools.reorderEventsByTimestamp(events);

        Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
        eventListAdapter = new EventAdapter(EventListActivity.this, android.R.layout.simple_list_item_multiple_choice, events);
        eventListAdapter.setOnTextClickIntent(intent);
        getListView().setAdapter(eventListAdapter);

        Thread distanceCalculatorThread = new Thread(new Runnable() {
            public void run() {
                List<EventInfo> changedEvents = new ArrayList<EventInfo>();
                for (EventInfo event : events) {
                    int distance = (int) Tools.getDistanceFromCurrentLocation(EventListActivity.this, event.getLatitude(), event.getLongitude());
                    if (event.getDistance() == null && distance != -1) {
                        event.setDistance(distance);
                        changedEvents.add(event);
                    } else if (event.getDistance() != null && distance != event.getDistance() && distance != -1) {
                        event.setDistance(distance);
                        changedEvents.add(event);
                    }
                }
                // Store the results for later on when the distance will not be available
                DBAdapter dbAdapter = Application.getInstance().getDBHelper(EventListActivity.this);
                dbAdapter.saveEntities(changedEvents);
                handler.post(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
                        eventListAdapter = new EventAdapter(EventListActivity.this, android.R.layout.simple_list_item_multiple_choice, events);
                        eventListAdapter.setOnTextClickIntent(intent);
                        getListView().setAdapter(eventListAdapter);
                        EventListActivity.this.getListView().invalidate();
                    }
                });
            }
        });
        distanceCalculatorThread.start();
    }
}
