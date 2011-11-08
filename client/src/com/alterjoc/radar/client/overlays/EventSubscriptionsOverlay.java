package com.alterjoc.radar.client.overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.Preferences;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * Displays circles on the map. Each category is represented with one circle, showing the radius in which
 * the user is subscribed to the events.
 * <p/>
 * User: Dejan
 * Date: 17.8.2010
 * Time: 13:46:05
 */
public class EventSubscriptionsOverlay extends Overlay {

    private GeoPoint usersLocation;
    private Preferences prefs;

    public void setUsersLocation(GeoPoint usersLocation) {
        this.usersLocation = usersLocation;
    }


    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {

        if (usersLocation != null){
            //---translate the GeoPoint to screen pixels---
            Point screenPts = new Point();
            mapView.getProjection().toPixels(usersLocation, screenPts);
            screenPts.set(screenPts.x, screenPts.y);

            // users current location:
            Preferences prefs = Application.getInstance().getPreferences(mapView.getContext());
            int radius = Integer.parseInt(prefs.getEffectiveRadiusValue());
            float radiusInPixels = mapView.getProjection().metersToEquatorPixels((float) radius);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(6);
            paint.setColor(Color.GREEN);
            paint.setAlpha(30);
            canvas.drawCircle(screenPts.x, screenPts.y, radiusInPixels, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(6);
            paint.setColor(Color.GREEN);
            paint.setAlpha(100);
            canvas.drawCircle(screenPts.x, screenPts.y, radiusInPixels, paint);
            paint.setStrokeWidth(2);
            paint.setColor(Color.BLACK);
            paint.setAlpha(30);
            canvas.drawCircle(screenPts.x, screenPts.y, radiusInPixels, paint);

            paint.setColor(Color.BLACK);
            paint.setAlpha(100);
            canvas.drawCircle(screenPts.x, screenPts.y, 3, paint);
            paint.setColor(Color.GRAY);
            canvas.drawCircle(screenPts.x, screenPts.y, 1, paint);            
        }
        return false;
    }


}
