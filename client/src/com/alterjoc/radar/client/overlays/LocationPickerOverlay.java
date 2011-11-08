package com.alterjoc.radar.client.overlays;

import android.graphics.drawable.Drawable;
import android.widget.Toast;
import com.alterjoc.radar.client.activities.locationpicker.LocationPickerMapView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * User: Dejan
 * Date: 21.9.2010
 * Time: 22:19:18
 */
public class LocationPickerOverlay extends ItemizedOverlay {

    private OverlayItem item;
    private MapView mapView;

    public LocationPickerOverlay(Drawable drawable, GeoPoint chosenLocation, MapView mapView) {
        super(boundCenterBottom(drawable));
        this.mapView = mapView;
        item = new OverlayItem(chosenLocation, "","");
        populate();
    }

    public void setSelectedLocation(GeoPoint geoPoint){
        item = new OverlayItem(geoPoint, "","");
        ((LocationPickerMapView)mapView).setChosenLocation(geoPoint);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return item;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        item = new OverlayItem(geoPoint, "","");
        populate();
        ((LocationPickerMapView)mapView).setChosenLocation(geoPoint);
        Toast toast = Toast.makeText(mapView.getContext(), geoPoint.toString(), Toast.LENGTH_LONG);
        toast.show();
        return super.onTap(geoPoint, mapView);
    }
}
