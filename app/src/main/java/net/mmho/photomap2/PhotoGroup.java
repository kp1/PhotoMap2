package net.mmho.photomap2;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.mmho.photomap2.geohash.GeoHash;

import java.util.ArrayList;

public class PhotoGroup extends ArrayList<HashedPhoto> implements Parcelable{
    public Marker marker;
    private GeoHash geoHash;
    private String address;
    private String description = "";

    public final static Parcelable.Creator<PhotoGroup> CREATOR = new Parcelable.Creator<PhotoGroup>(){

        @Override
        public PhotoGroup createFromParcel(Parcel in) {
            return new PhotoGroup(in);
        }

        @Override
        public PhotoGroup[] newArray(int size) {
            return new PhotoGroup[size];
        }
    };

    public PhotoGroup(Parcel in){
        in.readTypedList(this,HashedPhoto.CREATOR);
        geoHash = GeoHash.CREATOR.createFromParcel(in);
        address = in.readString();
        description = in.readString();

    }

    public PhotoGroup(HashedPhoto p){
        geoHash = p.getHash();
        add(p);
    }

    public void append(HashedPhoto p){
        if(!p.getHash().within(geoHash)) geoHash = geoHash.extend(p.getHash());
        add(p);
    }

    public void setAddress(String title,String description){
        address = title;
        this.description = description;
    }

    public String getTitle(){
        return address;
    }

    public String getDescription(){
        return description;
    }

    public LatLng getCenter(){
        return geoHash.getCenter();
    }
    public GeoHash getHash(){
        return geoHash;
    }

    public String locationToString() {
        LatLng p = geoHash.getCenter();
        return String.format("% 8.5f , % 8.5f", p.latitude,p.latitude);
    }

    public String toString(){
        return getDescription();
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeTypedList(this);
        geoHash.writeToParcel(out, flags);
        out.writeString(address);
        out.writeString(description);
    }

    static public float getMarkerColor(int size){
        float color;
        if(size>=100){
            color = BitmapDescriptorFactory.HUE_RED;
        }
        else if(size>=10){
            color = BitmapDescriptorFactory.HUE_ROSE;
        }
        else if(size>1){
            color = BitmapDescriptorFactory.HUE_ORANGE;
        }
        else{
            color = BitmapDescriptorFactory.HUE_GREEN;
        }
        return color;
    }
}
