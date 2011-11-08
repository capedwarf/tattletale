package com.alterjoc.radar.client.activities.locationpicker;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;

import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.R;
import com.alterjoc.radar.client.Tools;
import com.alterjoc.radar.client.overlays.LocationPickerOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;

/**
 * User: Dejan
 * Date: 21.9.2010
 * Time: 17:59:30
 */
public class LocationPickerActivity extends MapActivity {

    private LocationPickerOverlay overlay;
    private LocationPickerMapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        String address = (String)getIntent().getExtras().get("address");
        double longitude = getIntent().getExtras().getDouble("longitude");
        double latitude = getIntent().getExtras().getDouble("latitude");
        GeoPoint chosenLocation = new GeoPoint((int) (latitude * 1000000d), (int) (longitude * 1000000d));

        SharedPreferences settings = Tools.getAppPreferences(this);
        int zoom = settings.getInt("zoom", 10);
        
        mapView = new LocationPickerMapView(this, Application.getMapsKey(), chosenLocation);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setZoom(zoom);
        setContentView(mapView);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);


        mapView.getController().setCenter(chosenLocation);
        mapView.setAddress(address);

        Drawable drawable = mapView.getContext().getResources().getDrawable(R.drawable.current_location);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        overlay = new LocationPickerOverlay(drawable, chosenLocation, mapView);

        mapView.getOverlays().add(overlay);

    }

    public void setSelectedLocation(GeoPoint selectedGeoPoint){
        overlay.setSelectedLocation(selectedGeoPoint);
        mapView.getController().animateTo(selectedGeoPoint);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }


}
