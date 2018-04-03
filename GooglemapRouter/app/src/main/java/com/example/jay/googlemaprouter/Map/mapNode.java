package com.example.jay.googlemaprouter.Map;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by jay on 11/3/2017.
 */

public class mapNode {
    public  LatLng latLng;
    LinkedList<mapNode> childNodes = new LinkedList<>();

    public mapNode(){
        this.latLng = new LatLng(0,0);
    }

    public mapNode(LatLng mapNode)
    {
        this.latLng = mapNode;
    }

    public void addChild(mapNode child)
    {
        childNodes.add(child);
    }

    public double getDistance(mapNode otherNode)
    {
        return sqrt(pow((this.latLng.longitude - otherNode.latLng.longitude),2)+pow((this.latLng.latitude - otherNode.latLng.latitude),2));
    }

//    @Override
//    public boolean equals(Object o)
//    {
//        mapNode other = (mapNode)o;
//        return (this.latLng == other.latLng);
//    }
}
