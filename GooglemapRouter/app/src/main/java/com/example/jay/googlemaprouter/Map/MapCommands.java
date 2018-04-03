package com.example.jay.googlemaprouter.Map;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Node;

import java.util.ArrayList;

/**
 * Created by jay on 10/22/2017.
 */

public class MapCommands {

    public void addSource(LatLng pos,GoogleMap mMap)
    {
       // LatLng SJSU = new LatLng(37.3361179, -121.8817006);
        mMap.addCircle(new CircleOptions()
                .strokeWidth(4)
                .radius(4)
                .center(pos)
                .strokeColor(Color.parseColor("#B20000"))
                .fillColor(Color.parseColor("#FF1919")));
        mMap.addMarker(new MarkerOptions().position(pos).title("Marker in SJSU").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

    }

    public void addDestination(LatLng pos,GoogleMap mMap)
    {
        mMap.addMarker(new MarkerOptions().position(pos).title("Destination"));
    }


    public void drowline(ArrayList points, GoogleMap mMap)
    {
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.addAll(points);
        lineOptions.width(12);
        lineOptions.color(Color.BLUE);
        lineOptions.geodesic(true);
        mMap.addPolyline(lineOptions);

    }



}
