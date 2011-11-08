package com.alterjoc.radar.client;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;
import com.alterjoc.radar.client.database.DBAdapter;
import com.alterjoc.radar.common.data.EventInfo;

import java.util.List;

public class TozibabaUpdateWidgetService extends Service {
    @Override
    public void onStart(Intent intent, int startId) {
        // Build the widget update for today
        RemoteViews updateViews = buildUpdate(this);

        // Push update for this widget to the home screen
        ComponentName thisWidget = new ComponentName(this, TozibabaWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(thisWidget, updateViews);
    }

    public RemoteViews buildUpdate(Context context) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.tozibaba_widget);
        try{
            DBAdapter adapter = Application.getInstance().getDBHelper(context);

            String text = getResources().getString(R.string.channels_my_all) + adapter.countSubscribedTopics() + " / " + adapter.countAllTopics();
            text += getResources().getString(R.string.messages_active_acrhive) + (adapter.countAllEvents() - adapter.countArchivedEvents()) + " / " + adapter.countArchivedEvents();

            // Add last event, if there is one:
            List<EventInfo> events = adapter.getEvents(false);
            if (events != null && events.size() > 0){
                Tools.reorderEventsByTimestamp(events);

                EventInfo event = events.get(0);
                String title = event.getTitle();
                if (title != null && title.length() > 0){
                    text += getResources().getString(R.string.last_message) + title;
                } else {
                    text += getResources().getString(R.string.last_message) + event.getTopicInfo().getName();
                }
            }
            updateViews.setTextViewText(R.id.widget_text, text);
        } catch (Throwable e){
            /// do nothing
        }
        return updateViews;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to this service
        return null;
    }
}

