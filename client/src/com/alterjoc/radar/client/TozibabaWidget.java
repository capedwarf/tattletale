package com.alterjoc.radar.client;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * User: Dejan
 * Date: 22.10.2010
 * Time: 17:23:11
 */
public class TozibabaWidget extends AppWidgetProvider {

    public static String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.tozibaba_widget);

        Intent intent = new Intent(context, PostEventActivity.class);
        PendingIntent postEventPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        updateViews.setOnClickPendingIntent(R.id.wbutton, postEventPendingIntent);

        intent = new Intent(context, TozibabaService.class);
        intent.setAction(AbstractService.ACTION_SYNC);
        PendingIntent servicePendingIntent = PendingIntent.getService(context, 0, intent, 0);
        updateViews.setOnClickPendingIntent(R.id.refresh_button, servicePendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
        
        context.startService(new Intent(context, TozibabaUpdateWidgetService.class));
    }
}
