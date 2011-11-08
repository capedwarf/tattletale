package com.alterjoc.radar.client;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import com.alterjoc.radar.client.adapters.EventAdapter;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.common.data.EventInfo;

import java.util.List;

/**
 * User: Dejan
 * Date: 16.10.2010
 * Time: 10:56:26
 */
public class EventArchiveListActivity extends ListActivity {
    private EventAdapter eventListAdapter;
    private List<EventInfo> events;
    private boolean allSelected;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);

        getListView().setItemsCanFocus(false);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);
    }
    @Override
    protected void onResume() {
        super.onResume();
        DBAdapter dbAdapter = Application.getInstance().getDBHelper(this);
        events = dbAdapter.getNonFilteredEvents(true);
        Tools.reorderEventsByTimestamp(events);
        eventListAdapter = new EventAdapter(this, android.R.layout.simple_list_item_multiple_choice, events);
        eventListAdapter.setButtonsEnabled(false);
        getListView().setAdapter(eventListAdapter);
        Intent intent = new Intent(EventArchiveListActivity.this, EventDetailsActivity.class);
        eventListAdapter.setOnTextClickIntent(intent);        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem menuItem = menu.add(1, 1, 1, "Radar");
        menuItem.setIcon(Tools.getImage(this, "radar"));
        Intent intent = new Intent(EventArchiveListActivity.this, EventsMapActivity.class);
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

        menuItem = menu.add(1, 3, 3, R.string.reactivate);
        menuItem.setIcon(Tools.getImage(this, "set_archived"));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                List<EventInfo> checkedEvents = eventListAdapter.getCheckedEvents();
                for (EventInfo checkedEvent : checkedEvents) {
                    checkedEvent.setArchived(false);
                    checkedEvent.setUnarchived(true);
                }
                DBAdapter adapter = Application.getInstance().getDBHelper(EventArchiveListActivity.this);
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
                DBAdapter adapter = Application.getInstance().getDBHelper(EventArchiveListActivity.this);
                adapter.deleteEntities(checkedEvents);
                eventListAdapter.notifyDataSetChanged();

                int count = getListView().getAdapter().getCount();
                for (int i = 0; i < count; i++) {
                    getListView().setItemChecked(i, false);
                }

                return true;
            }
        });

        intent = new Intent(EventArchiveListActivity.this, EventListActivity.class);
        menuItem = menu.add(1, 5, 5, R.string.list);
        menuItem.setIcon(Tools.getImage(this, "menulist"));
        menuItem.setIntent(intent);

        menuItem = menu.add(1, 6, 6, "Filter");
        menuItem.setIcon(Tools.getImage(this, "filter"));
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                Tools.AfterFilterChangeAction action = new Tools.AfterFilterChangeAction() {
                    public void doAction() {
                        DBAdapter dbAdapter = Application.getInstance().getDBHelper(EventArchiveListActivity.this);
                        events = dbAdapter.getNonFilteredEvents(true);
                        Tools.reorderEventsByTimestamp(events);

                        Intent intent = new Intent(EventArchiveListActivity.this, EventDetailsActivity.class);
                        eventListAdapter = new EventAdapter(EventArchiveListActivity.this, android.R.layout.simple_list_item_multiple_choice, events);
                        eventListAdapter.setOnTextClickIntent(intent);
                        getListView().setAdapter(eventListAdapter);
                    }
                };

                Tools.showFilterDialog(EventArchiveListActivity.this, action);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}
