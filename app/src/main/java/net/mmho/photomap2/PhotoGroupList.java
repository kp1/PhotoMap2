package net.mmho.photomap2;

import android.location.Location;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PhotoGroupList extends ArrayList<PhotoGroup>{
    public final static String EXTRA_GROUP="group";
    public static final String EXTRA_INDEX = "index";
    public static final int MESSAGE_RESTART = 0;
    public static final int MESSAGE_ADD = 1;
    final PhotoCursor mCursor;
    private float distance;

    PhotoGroupList(PhotoCursor c){
        mCursor = c;
    }

    public PhotoGroupList exec(float distance,Handler handler,CancellationSignal signal){
        clear();
        this.distance = distance;
        if(!mCursor.moveToFirst()) return this;

        if(handler!=null){
            Message message = new Message();
            message.what = MESSAGE_RESTART;
            handler.sendMessage(message);
        }

        do{
            int i;
            for(i=0;i<this.size();i++){
                if(signal!=null)signal.throwIfCanceled();
                LatLng p = mCursor.getLocation();
                LatLng c = get(i).getCenter();
                float[] d = new float[3];
                Location.distanceBetween(p.latitude,p.longitude,c.latitude,c.longitude,d);
                if(d[0]<distance){
                    get(i).append(p,mCursor.getID());
                    break;
                }
            }
            if(i==this.size()){
                PhotoGroup g = new PhotoGroup(mCursor.getLocation(), mCursor.getID());
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
        }while(mCursor.moveToNext());
        return this;
    }

    public boolean equals(PhotoGroupList photoGroup){
        if(photoGroup.size()!=size()) return false;
        for(int i=0;i<size();i++){
            if(!get(i).equals(photoGroup.get(i))) return false;
        }
        return true;
    }

    float getDistance(){
        return distance;
    }

}
