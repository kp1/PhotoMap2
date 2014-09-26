package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoGroupList extends ArrayList<PhotoGroup>{
    public final static String EXTRA_GROUP="group";
    public static final int MESSAGE_RESTART = 0;
    public static final int MESSAGE_ADD=1;
    public static final int MESSAGE_APPEND = 2;
    public static final int MESSAGE_ADDRESS = 3;
    private static final int ADDRESS_MAX_RESULTS = 1;

    private float distance;
    private boolean finished;

    PhotoGroupList(){
        clear();
        distance = 0;
        finished = false;
    }

    public PhotoGroupList exec(PhotoCursor cursor,float distance,boolean geocode,Context context,Handler handler,CancellationSignal signal){
        clear();

        finished = false;
        this.distance = distance;

        if(cursor==null || !cursor.moveToFirst()) return this;

        if(handler!=null){
            Message message = new Message();
            message.what = MESSAGE_RESTART;
            handler.sendMessage(message);
        }

        do{
            int i;
            for(i=0;i<this.size();i++){
                if(signal!=null)signal.throwIfCanceled();
                LatLng p = cursor.getLocation();
                boolean contains = get(i).getArea().contains(p);
                if(!contains) {
                    LatLng c = get(i).getCenter();
                    float[] d = new float[3];
                    Location.distanceBetween(p.latitude, p.longitude, c.latitude, c.longitude, d);
                    if (d[0] < distance) {
                        contains = true;
                    }
                }
                if(contains){
                    get(i).append(p, cursor.getID());
                    if (handler != null) {
                        handler.sendEmptyMessage(MESSAGE_APPEND);
                    }
                    break;
                }
            }
            if(i==this.size()){
                PhotoGroup g = new PhotoGroup(cursor.getLocation(),cursor.getID());
                add(g);
                if(handler!=null){
                    Bundle b = new Bundle();
                    b.putParcelable(EXTRA_GROUP,g);
                    Message message = new Message();
                    message.setData(b);
                    message.what = MESSAGE_ADD;
                    handler.sendMessage(message);
                }
            }
        }while(cursor.moveToNext());

        if(!geocode){
            finished = true;
            return this;
        }

        Geocoder g = new Geocoder(context);
        for(PhotoGroup group:this){
            if(signal!=null)signal.throwIfCanceled();
            LatLng location = group.getCenter();
            List<Address> addresses;
            try {
                addresses = g.getFromLocation(location.latitude, location.longitude, ADDRESS_MAX_RESULTS);
                if(addresses!=null && addresses.size()>0){
                    group.address = addresses.get(0);
                    if(handler!=null) handler.sendEmptyMessage(MESSAGE_ADDRESS);
                }
            } catch (IOException e) {
                // do nothing
            }

        }

        finished = true;

        return this;
    }

    public boolean equals(PhotoGroupList photoGroup){
        if(photoGroup.size()!=size()) return false;
        for(int i=0;i<size();i++){
            if(!get(i).equals(photoGroup.get(i))) return false;
        }
        return true;
    }

    public boolean getFinished(){
        return finished;
    }
    public float getDistance(){
        return distance;
    }

}
