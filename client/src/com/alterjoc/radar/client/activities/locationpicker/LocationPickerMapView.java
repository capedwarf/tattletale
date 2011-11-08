package com.alterjoc.radar.client.activities.locationpicker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.alterjoc.radar.client.Tools;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

/**
 * User: Dejan
 * Date: 23.9.2010
 * Time: 20:50:48
 */
public class LocationPickerMapView extends MapView {
    private EditText addressET;
    private GeoPoint chosenLocation;
    private boolean isSatelliteViewOn;

    public LocationPickerMapView(Context context, String s, GeoPoint chosenLocation) {
        super(context, s);
        this.chosenLocation = chosenLocation;
        ImageButton okButton = new ImageButton(context);
        okButton.setImageDrawable(getResources().getDrawable(Tools.getImage(context, "ok")));
        okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("address", addressET.getText().toString());
                intent.putExtra("longitude", LocationPickerMapView.this.chosenLocation.getLongitudeE6());
                intent.putExtra("latitude", LocationPickerMapView.this.chosenLocation.getLatitudeE6());
                ((LocationPickerActivity)getContext()).setResult(1, intent);
                ((LocationPickerActivity)getContext()).finish();
            }
        });
        ImageButton resetButton = new ImageButton(context);
        resetButton.setImageDrawable(getResources().getDrawable(Tools.getImage(context, "resetLocation")));
        resetButton.setOnClickListener(new OnClickListener(){
            public void onClick(View view) {
                Location location = Tools.getCurrentLocation(getContext());
                if (location != null){
                    GeoPoint geoPoint = new GeoPoint((int)(location.getLatitude() * 1000000), (int)(location.getLongitude() * 1000000));
                    ((LocationPickerActivity)getContext()).setSelectedLocation(geoPoint);
                }
            }
        });

        ImageButton changeViewButton = new ImageButton(getContext());
        changeViewButton.setImageDrawable(this.getResources().getDrawable(Tools.getImage(getContext(), "switch")));
        changeViewButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                isSatelliteViewOn = !isSatelliteViewOn;
                LocationPickerMapView.this.setSatellite(isSatelliteViewOn);
            }
        });

        SharedPreferences prefer = Tools.getAppPreferences(getContext());
        String address = prefer.getString("address", "");
        addressET = new EditText(context);
        addressET.setTextSize(Tools.getButtonsFontSize(context));
        addressET.setSingleLine(true);
        addressET.setText(address);
        addressET.setSelection(0);

        LinearLayout topWrapperLayout = new LinearLayout(context);
        topWrapperLayout.setOrientation(LinearLayout.HORIZONTAL);
        topWrapperLayout.addView(okButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topWrapperLayout.addView(resetButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topWrapperLayout.addView(changeViewButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        topWrapperLayout.addView(addressET, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        addView(topWrapperLayout, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setAddress(String text) {
        addressET.setText(text);
        addressET.setSelection(0);
    }

    public void setChosenLocation(GeoPoint chosenLocation){
        setAddress(Tools.getAddress(chosenLocation.getLatitudeE6() / 1000000d, chosenLocation.getLongitudeE6() / 1000000d, this.getContext()));
        getController().animateTo(chosenLocation);
        this.chosenLocation = chosenLocation;
    }
}
