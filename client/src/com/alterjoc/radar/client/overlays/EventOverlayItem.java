package com.alterjoc.radar.client.overlays;

import android.graphics.drawable.Drawable;
import com.alterjoc.radar.common.data.EventInfo;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * User: Dejan
 * Date: 6.9.2010
 * Time: 17:52:00
 */
public class EventOverlayItem extends OverlayItem {

    private Drawable defaultMarker;
    private EventInfo event;

    public EventOverlayItem(GeoPoint geoPoint, String title, String snippet, EventInfo event) {
        super(geoPoint, title, snippet);
        this.event = event;
    }

    public Drawable getDefaultMarker() {
        return defaultMarker;
    }

    public void setDefaultMarker(Drawable defaultMarker) {
        this.defaultMarker = defaultMarker;
    }

    public EventInfo getEvent() {
        return event;
    }

    public void setEvent(EventInfo event) {
        this.event = event;
    }
}