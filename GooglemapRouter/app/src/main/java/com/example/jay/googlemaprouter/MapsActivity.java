package com.example.jay.googlemaprouter;

import com.example.jay.googlemaprouter.Bluetooth.*;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.jay.googlemaprouter.Map.MapCommands;
import com.example.jay.googlemaprouter.Map.mapGraph;
import com.example.jay.googlemaprouter.Map.mapNode;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener{

    boolean flag = true;
    private GoogleMap mMap;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothConnect bt_Thread;
    BluetoothSocket mmSocket;
    BluetoothCommunicate ble_communication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bluethoot_enable();
    }

    public void bluethoot_enable() {
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(MapsActivity.this,"Your Device doesnot support Bluetooth", Toast.LENGTH_LONG).show();
        }
        else if (!mBluetoothAdapter.isEnabled()) {
              Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
              startActivityForResult(enableBtIntent, 1);
            // mBluetoothAdapter.enable();
            Toast.makeText(MapsActivity.this,"Bluethooth Enabled", Toast.LENGTH_LONG).show();
        } else
            Toast.makeText(MapsActivity.this, "Bluethooth is already on", Toast.LENGTH_LONG).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
       LatLng SJSU = new LatLng(37.3361179, -121.8817006);
        //Drawable circleDrawable = getResources().getDrawable(R.drawable);
       // BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
        mMap.addCircle(new CircleOptions()
                .strokeWidth(4)
                .radius(4)
                .center(SJSU)
                .strokeColor(Color.parseColor("#B20000"))
                .fillColor(Color.parseColor("#FF1919")));
        mMap.addMarker(new MarkerOptions().position(SJSU).title("Marker in SJSU").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SJSU,18));
          //  connect_alpha();

       // mMap.moveCamera(CameraUpdateFactory.newLatLng(SJSU));
        initListeners();
    }

    public void goStop(View view)
    {
        if(!mBluetoothAdapter.isEnabled())
        {
            Toast.makeText(MapsActivity.this,"Enable Bluetooth",Toast.LENGTH_LONG).show();
        }
        else if(mmSocket==null)
        {
            Toast.makeText(MapsActivity.this,"Connect To Alpha",Toast.LENGTH_LONG).show();
        }
//        else if(!mmSocket.isConnected())
//        {
//            Toast.makeText(MapsActivity.this,"Connect To Alpha",Toast.LENGTH_LONG).show();
//        }
        else
        {
            mapGraph graph = new mapGraph();
            mapNode destination = new mapNode(new LatLng(37.335799,-121.882925));
            mapNode source = new mapNode(new LatLng(37.334918,-121.883938));
            ArrayList<LatLng> route = graph.maproute(source,destination);

            mMap.clear();
            MapCommands mapCommands = new MapCommands();
            mapCommands.addSource(route.get(route.size()-1),mMap);
            mapCommands.addDestination(route.get(0),mMap);
            mapCommands.drowline(route,mMap);
            //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.get(route.size()-1),18));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(route.get(route.size()-1),18));
            byte[] buffer = new byte[24];
            final Button go_stop = (Button) findViewById(R.id.button1);
            if(flag)
            {
                Toast.makeText(MapsActivity.this,"Sending Check Points", Toast.LENGTH_LONG).show();
                for(int i=0;i<route.size();i++)
                {
                    String str = "#"+route.get(i).latitude+"@"+route.get(i).longitude+"/n";
                    buffer = str.getBytes();
                    ble_communication.write(buffer);
                }
                Toast.makeText(MapsActivity.this,"Done !", Toast.LENGTH_LONG).show();

                buffer = "*1".getBytes();
                ble_communication.write(buffer);
                go_stop.setText("STOP");
                flag = false;
            }
            else
            {
                buffer = "*0".getBytes();
                ble_communication.write(buffer);
                go_stop.setText("GO");
                flag = true;
            }
        }
    }


    // Setting onclick event listener for the map
    @Override
    public void onMapClick(LatLng latLng) {
        //mMap.clear();
       /* LatLng SJSU = new LatLng(37.3361179, -121.8817006);
        mMap.addCircle(new CircleOptions()
                .strokeWidth(4)
                .radius(4)
                .center(SJSU)
                .strokeColor(Color.parseColor("#B20000"))
                .fillColor(Color.parseColor("#FF1919")));
        mMap.addMarker(new MarkerOptions().position(SJSU).title("Marker in SJSU").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

       // Toast.makeText(MapsActivity.this, "Latitude is: "+latLng.latitude+", Longtitude is: "+latLng.longitude, Toast.LENGTH_LONG).show();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Destination"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));

        ArrayList points = new ArrayList();
        points.add(SJSU);
        points.add(latLng);

        MapCommands mapCommands = new MapCommands();
        mapCommands.drowline(points,mMap);*/

       // ble_communication.read();
//           byte[] buffer = new byte[24];
//            buffer = "#34.123456@-121.123456\n".getBytes();
//            ble_communication.write(buffer);
    }

    /*
    This function will add listener
     */
    private void initListeners() {
        mMap.setOnMapClickListener(this);

    }

    public void connect_alpha(View view) {
       // bluethoot_enable();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals("HC-05")) {
                Log.i("Alpha", "got Alpha");

              //  Parcelable[] uuidExtra = (ParcelUuid[]) device.getUuids();

               /* for (Parcelable Dev_uuid : uuidExtra) {
                    Log.i("UUID : ", Dev_uuid.toString());
                }*/

                bt_Thread = new BluetoothConnect(device,mBluetoothAdapter);
                mmSocket = bt_Thread.get_socket();
               // bt_Thread.start();
                Log.i("Bluetooth Socket", "Socket stored : "+mmSocket.toString());
                ble_communication = new BluetoothCommunicate(mmSocket);
                break;
            }
        }

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try {
            mmSocket.close();
            mBluetoothAdapter.disable();
        } catch (IOException closeException) {
            Log.i("BLE_Connect","BLE_connect Exception"+closeException);
        }
    }


}