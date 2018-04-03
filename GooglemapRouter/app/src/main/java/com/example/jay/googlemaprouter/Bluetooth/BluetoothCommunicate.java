package com.example.jay.googlemaprouter.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jay on 10/22/2017.
 */

public class BluetoothCommunicate{
        private final BluetoothSocket mmSocket;
        private  InputStream mmInStream;
        private  OutputStream mmOutStream;
        byte[] buffer = new byte[12];

        public BluetoothCommunicate(BluetoothSocket socket) {
            mmSocket = socket;
        }

    public LatLng read() {
        InputStream inputStream =  null;
        Log.i("Bluetooth","Start Reading");
                try {

                     inputStream = mmSocket.getInputStream();

                } catch (IOException e) {
                    Log.i("Bluetooth", "Exception received data : " + e);
                }
                LatLng currentPos = readStrToLalLong(inputStream);
                return currentPos;
    }


    public LatLng readStrToLalLong(InputStream inputStream)
    {
        String in_str;

        do
        {
            try
            {

                if( inputStream.available()==0)
                {
                    return null;
                }
                inputStream.read(buffer,0,1);
            }
            catch (Exception e)
            {
               // Log.e("Ble Read","Exception: "+e);
                return null;
            }
            in_str = new String(buffer);
            // Log.i("Bluetooth","received data : waiting For @ "+in_str.substring(0,1));
        }while(!in_str.substring(0,1).equals("#"));

        in_str = "";
        String str;
        do {

            try
            {

                if( inputStream.available()==0)
                {
                    return null;
                }
                inputStream.read(buffer,0,1);
            }
            catch (Exception e)
            {
               // Log.e("Ble Read","Exception: "+e);
                return null;
            }
            str = new String(buffer);
            in_str= in_str.concat(str.substring(0,1));
            // Log.i("Bluetooth","received data : waiting For # "+str.substring(0,1));
        }while(!str.substring(0,1).equals("@"));

     //   Log.i("Bluetooth","received data : Latitude "+in_str.substring(0,in_str.length()-1));
        double curr_latitude = Double.parseDouble(in_str.substring(0,in_str.length()-1));
        in_str = "";
        do {

            try
            {
                if( inputStream.available()==0)
                {
                    return null;
                }
                inputStream.read(buffer,0,1);
            }
            catch (Exception e)
            {
       //         Log.e("Ble Read","Exception: "+e);
                return null;
            }

            str = new String(buffer);
            in_str = in_str.concat(str.substring(0,1));


            //Log.i("Bluetooth","received data : waiting For new line "+str.substring(0,1));
        }while(!str.substring(0,1).equals("\n"));

       // Log.i("Bluetooth","received data : Longitude "+in_str.substring(0,in_str.length()-1));
        double curr_longitude = Double.parseDouble(in_str.substring(0,in_str.length()-1));
        LatLng latLng = new LatLng(curr_latitude,curr_longitude);

        return  latLng;
    }

    public void write(byte[] bytes) {
        try {
            mmOutStream = mmSocket.getOutputStream();
            mmOutStream.write(bytes);
            Log.i("Bluetooth","Transmitted ");
        } catch (IOException e) {
            Log.i("Bluetooth","Exception Write data : "+e);
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

}
