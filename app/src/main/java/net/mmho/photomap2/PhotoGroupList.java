package net.mmho.photomap2;

import android.app.LoaderManager;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PhotoGroupList extends ArrayList<PhotoGroup> {
    final PhotoCursor mCursor;

    PhotoGroupList(PhotoCursor c){
        mCursor = c;
    }

    void exec(float distance){
        this.clear();
        if(!mCursor.moveToFirst()) return;

        do{
            int i;
            for(i=0;i<this.size();i++){
                LatLng p = mCursor.getLocation();
                LatLng c = this.get(i).getCenter();
                float[] d = new float[3];
                Location.distanceBetween(p.latitude,p.longitude,c.latitude,c.longitude,d);
                if(d[0]<distance){
                    this.get(i).append(p,mCursor.getID());
                    break;
                }
            }
            if(i==this.size()){
                PhotoGroup g = new PhotoGroup(mCursor.getLocation(), mCursor.getID());
                this.add(g);
            }
        }while(mCursor.moveToNext());
    }

    boolean equals(PhotoGroupList photoGroup){
        if(photoGroup.size()!=size()) return false;
        for(int i=0;i<size();i++){
            if(!get(i).equals(photoGroup.get(i))) return false;
        }
        return true;
    }



}
