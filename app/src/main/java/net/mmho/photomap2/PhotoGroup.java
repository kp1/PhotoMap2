package net.mmho.photomap2;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.ArrayList;

public class PhotoGroup extends ArrayList<PhotoGroup.Group> {
    final PhotoCursor mCursor;

    PhotoGroup(PhotoCursor c){
        mCursor = c;
    }

    public class Group extends ArrayList<Long> implements Serializable{
        public Marker marker;
        private LatLngBounds area;
        Group(LatLng p,long id){
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            b.include(p);
            area = b.build();
            this.add(id);
        }

        public LatLng getCenter(){
            return area.getCenter();
        }
        public LatLngBounds getArea(){
            return area;
        }
        public String toString() {
            LatLng c = area.getCenter();
            return String.format("% 8.5f , % 8.5f",c.latitude,c.longitude);
        }

        void append(LatLng point,long id){
            area = area.including(point);
            this.add(id);
        }

        public boolean equals(Group g) {
            if(size()!=g.size()) return false;
            if(getArea().equals(g.getArea())) return false;
            for(int i=0;i<size();i++){
                if(!get(i).equals(g.get(i))) return false;
            }
            return true;
        }
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
                Group g = new Group(mCursor.getLocation(), mCursor.getID());
                this.add(g);
            }
        }while(mCursor.moveToNext());
    }

    boolean equals(PhotoGroup photoGroup){
        if(photoGroup.size()!=size()) return false;
        for(int i=0;i<size();i++){
            if(!get(i).equals(photoGroup.get(i))) return false;
        }
        return true;
    }



}
