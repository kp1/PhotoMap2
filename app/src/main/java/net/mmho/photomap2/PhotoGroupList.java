package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

public class PhotoGroupList extends ArrayList<PhotoGroup>{
    public static final int MESSAGE_RESTART = 0;
    public static final int MESSAGE_APPEND = 1;
    public static final int MESSAGE_ADDRESS = 2;
    private static final int ADDRESS_MAX_RESULTS = 1;

    private static final int HASH_CHARACTER_LENGTH=9;

    private float distance;
    private boolean finished;
    private boolean cancel;

    PhotoGroupList(){
        clear();
        distance = 0;
        finished = false;
    }

    public PhotoGroupList exec(PhotoCursor cursor,int distance,boolean geocode,Context context,Handler handler)
        throws CancellationException{
        clear();

        finished = false;
        cancel = false;
        this.distance = distance;

        if(cursor==null || !cursor.moveToFirst()) return this;

        if(handler!=null) handler.sendEmptyMessage(MESSAGE_RESTART);

        do{
            GeoHash hash = cursor.getGeoHash(HASH_CHARACTER_LENGTH);
            boolean isBreak=false;
            for(PhotoGroup g:this){
                if(cancel)throw new CancellationException("cancel grouping.");
                int insignificantBits = 64-distance;
                boolean contains = (hash.longValue()^g.getArea().longValue())>>>insignificantBits==0;

                if(contains){
                    g.append(cursor.getID(),hash);
                    isBreak=true;
                    break;
                }
            }
            if(!isBreak){
                PhotoGroup g = new PhotoGroup(hash,cursor.getID());
                add(g);
            }
            if (handler != null) handler.sendEmptyMessage(MESSAGE_APPEND);
        }while(cursor.moveToNext());

        if(!geocode){
            finished = true;
            return this;
        }

        Geocoder g = new Geocoder(context);
        for(PhotoGroup group:this){
            if(cancel)throw new CancellationException("cancel grouping.");
            WGS84Point p = group.getCenter();
            List<Address> addresses;
            try {
                addresses = g.getFromLocation(p.getLatitude(),p.getLongitude(), ADDRESS_MAX_RESULTS);
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

    public void cancel(){
        cancel = true;
    }

    public boolean isFinished(){
        return finished;
    }
    public void reset(){
        finished = false;
    }
    public float getDistance(){
        return distance;
    }

}
