package net.mmho.photomap2;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.model.LatLng;

import net.mmho.photomap2.geohash.GeoHash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

public class PhotoGroupList extends ArrayList<PhotoGroup>{
    public static final int MESSAGE_RESTART = 0;
    public static final int MESSAGE_APPEND = 1;
    public static final int MESSAGE_ADDRESS = 2;
    private static final int ADDRESS_MAX_RESULTS = 1;
    public static final String PROGRESS_ACTION = "PROGRESS";
    public static final String LOADER_STATUS = "STATUS";

    private float distance;
    private boolean finished;
    private boolean cancel;

    PhotoGroupList(){
        clear();
        distance = 0;
        finished = false;
    }

    public PhotoGroupList exec(ArrayList<HashedPhoto> list,int distance,boolean geocode,Context context)
        throws CancellationException{
        clear();

        finished = false;
        cancel = false;
        this.distance = distance;

        Intent intent = new Intent(PROGRESS_ACTION);
        intent.putExtra(LOADER_STATUS,MESSAGE_RESTART);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        for(HashedPhoto p:list){
            GeoHash hash = p.getHash();
            boolean isBreak=false;
            for(PhotoGroup g:this){
                if(cancel)throw new CancellationException("cancel grouping.");
                if(hash.within(g.getHash(),distance)){
                    g.append(p);
                    isBreak=true;
                    break;
                }
            }
            if(!isBreak){
                PhotoGroup g = new PhotoGroup(p);
                add(g);
            }
            intent = new Intent(PROGRESS_ACTION).putExtra(LOADER_STATUS,MESSAGE_APPEND);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        if(!geocode){
            finished = true;
            return this;
        }

        Geocoder g = new Geocoder(context);
        for(PhotoGroup group:this){
            if(cancel)throw new CancellationException("cancel grouping.");
            AddressRecord record = AddressRecord.getAddressByHash(group.getHash());
            if(record!=null){
                group.setAddress(record.getTitle(),record.getDescription());
                continue;
            }
            LatLng p = group.getCenter();
            List<Address> addresses;
            try {
                addresses = g.getFromLocation(p.latitude,p.longitude, ADDRESS_MAX_RESULTS);
                if(addresses!=null && addresses.size()>0){
                    Address a = addresses.get(0);
                    group.setAddress(AddressUtil.getTitle(a,context),AddressUtil.getDescription(a));
                    intent = new Intent(PROGRESS_ACTION).putExtra(LOADER_STATUS,MESSAGE_ADDRESS);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    new AddressRecord(AddressUtil.getTitle(a,context),AddressUtil.getDescription(a),group.getHash()).save();
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
