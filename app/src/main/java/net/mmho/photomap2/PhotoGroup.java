package net.mmho.photomap2;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBoundsCreator;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class PhotoGroup extends ArrayList<PhotoGroup.Group> {
    final PhotoCursor mCursor;

    PhotoGroup(PhotoCursor c){
        mCursor = c;
    }

    public class Group implements Parcelable{
        public Marker marker;
        private LatLngBounds area;
        private ArrayList<Long> id_list = new ArrayList<Long>();

        public final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>(){

            @Override
            public Group createFromParcel(Parcel in) {
                final Group g = new Group(in);
                return g;
            }

            @Override
            public Group[] newArray(int size) {
                return new Group[0];
            }
        };

        Group(Parcel in){
            in.readList(id_list, null);
            area = LatLngBounds.CREATOR.createFromParcel(in);
        }

        Group(LatLng p,long id){
            LatLngBounds.Builder b = new LatLngBounds.Builder();
            b.include(p);
            area = b.build();
            id_list.add(id);
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

        public int size(){
            return id_list.size();
        }

        public long getID(int index){
            return id_list.get(index);
        }

        void append(LatLng point,long id){
            area = area.including(point);
            id_list.add(id);
        }

        public boolean equals(Group g) {
            return id_list.equals(g.id_list) && area.equals(g.area);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeList(id_list);
            area.writeToParcel(out,0);

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
