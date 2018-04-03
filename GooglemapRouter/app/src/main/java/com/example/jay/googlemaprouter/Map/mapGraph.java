package com.example.jay.googlemaprouter.Map;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import static java.lang.Math.abs;


public class mapGraph {
    public ArrayList<mapNode> v = new ArrayList<mapNode>();

    public mapGraph()
    {
        mapNode lib = new mapNode(new LatLng(37.335244,-121.884568));
        mapNode lib1 = new mapNode(new LatLng(37.3352422,-121.884381));
        mapNode libRD= new mapNode(new LatLng(37.335362,-121.884257));
        mapNode libRD1 = new mapNode(new LatLng(37.335567,-121.884396));
        mapNode libRD2 = new mapNode(new LatLng(37.335533,-121.883914));
        mapNode libRD11 = new mapNode(new LatLng(37.335721,-121.884106));
        mapNode libRD21 = new mapNode(new LatLng(37.335501,-121.883442));
        mapNode libRD211 = new mapNode(new LatLng(37.335498,-121.883366));
        mapNode libRD22 = new mapNode(new LatLng(37.335494,-121.883298));
        mapNode libRD221 = new mapNode(new LatLng(37.335493,-121.883269));
        mapNode libRD23 = new mapNode(new LatLng(37.335522,-121.883157));
        mapNode libRD24 = new mapNode(new LatLng(37.335581,-121.883044));
        mapNode libRD222 = new mapNode(new LatLng(37.335816,-121.883983));
        mapNode libRD25 = new mapNode(new LatLng(37.335658,-121.882883));

        lib.addChild(lib1);
        lib1.addChild(lib);

        lib1.addChild(libRD);
        libRD.addChild(lib1);

        libRD.addChild(libRD1);
        libRD1.addChild(libRD);

        libRD.addChild(libRD2);
        libRD2.addChild(libRD);

        libRD1.addChild(libRD11);
        libRD11.addChild(libRD1);

        libRD2.addChild(libRD21);
        libRD21.addChild(libRD2);

        libRD21.addChild(libRD211);
        libRD211.addChild(libRD21);

        libRD211.addChild(libRD22);
        libRD22.addChild(libRD211);

        libRD22.addChild(libRD221);
        libRD221.addChild(libRD22);

        libRD221.addChild(libRD23);
        libRD23.addChild(libRD221);

        libRD23.addChild(libRD24);
        libRD24.addChild(libRD23);

        libRD22.addChild(libRD222);
        libRD222.addChild(libRD22);

        libRD24.addChild(libRD25);
        libRD25.addChild(libRD24);

        v.add(lib);         //0
        v.add(lib1);        //1
        v.add(libRD);       //2
        v.add(libRD1);      //3
        v.add(libRD2);      //4
        v.add(libRD11);     //5
        v.add(libRD21);     //6
        v.add(libRD22);     //7
        v.add(libRD23);     //8
        v.add(libRD24);     //9
        v.add(libRD211);    //10
        v.add(libRD221);    //11
        v.add(libRD222);    //12
        v.add(libRD25);     //13

    }

    public ArrayList<LatLng> maproute(mapNode source,mapNode destination)
    {
        ArrayList<LatLng> route = new ArrayList<LatLng>();
        ArrayList<mapNode> nodeList = new ArrayList<mapNode>();
        ArrayList<Integer> parentNodeList = new ArrayList<Integer>();
        ArrayList<Double> distance = new ArrayList<Double>();
        ArrayList<Boolean> visited = new ArrayList<Boolean>();

        mapNode node = source;
        nodeList.add(node);
        distance.add(0.0);
        visited.add(true);
        parentNodeList.add(0);
        int num = 0;

        mapNode nearDest = nearestCheckpoints(destination,source);


        if(!(source.getDistance(destination) < source.getDistance(nearDest)))
        {

            node = nearestCheckpoints(source,destination);
            if(node != nodeList.get(0))
            {
                nodeList.add(node);
                distance.add(source.getDistance(node));
                visited.add(true);
                parentNodeList.add(num);
                num = 1;
            }

            while(!node.latLng.equals(nearDest.latLng))
            {
                //Log.d("Node", "maproute: next node "+node.latLng.toString());
                for(int i=0;i<node.childNodes.size();i++)
                {
                    if(!nodeList.contains(node.childNodes.get(i)))
                    {
                        nodeList.add(node.childNodes.get(i));
                        distance.add(distance.get(num)+node.getDistance(node.childNodes.get(i)));
                        visited.add(false);
                        parentNodeList.add(num);
                    }
                    else
                    {
                        if(distance.get(nodeList.indexOf(node.childNodes.get(i)))>distance.get(num)+node.getDistance(node.childNodes.get(i)))
                        {
                            distance.set(nodeList.indexOf(node.childNodes.get(i)),distance.get(num)+node.getDistance(node.childNodes.get(i)));
                            parentNodeList.add(num);
                        }
                    }
                }

                num = minDistance(distance,visited);
                node = nodeList.get(num);
                visited.set(num,true);
            }

        }

        if(nearDest!=destination)
        {
            nodeList.add(destination);
            parentNodeList.add(num);
            num = nodeList.size()-1;
        }


        try
        {
            route = mapConnectRoute(nodeList,parentNodeList,num);
        }catch(RuntimeException expectedException) {
            Log.e("Routing Exception",expectedException.getMessage());
            route.add(source.latLng);
        }

        return route;
    }

    public ArrayList<LatLng> mapConnectRoute(ArrayList<mapNode> nodeList, ArrayList<Integer> parentList,int num)
    {
        ArrayList route = new ArrayList();

        if((nodeList.size()==0) | (parentList.size()==0) | (num<0)) return route;
        while(num!=0)
        {
            if(num>=nodeList.size())
            {
                throw new RuntimeException("Invalid Routing");
            }
            route.add(nodeList.get(num).latLng);
            num = parentList.get(num);
        }
        route.add(nodeList.get(num).latLng);
        return route;
    }

    public mapNode nearestCheckpoints(mapNode val1,mapNode val2)
    {
        int index = -1;
        double min = Double.MAX_VALUE;

        for(int i=0;i<v.size();i++)
        {
            if(min > (v.get(i).getDistance(val1)))
            {
                if(v.get(i).getDistance(val1) <= val2.getDistance(val1))
                    min = v.get(i).getDistance(val1);
                    index = i;
            }
        }
        return v.get(index);
    }

    public int minDistance(ArrayList<Double> distance,ArrayList<Boolean> visited)
    {
        double min = Double.MAX_VALUE;
        int index = -1;
        for(int i=0;i<distance.size();i++)
        {
            if((min > distance.get(i)) && (visited.get(i)==false))
            {
                min = distance.get(i);
                index = i;
            }

        }
        return index;
    }


}
