package net.mmho.photomap2;

import android.location.Address;
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

    public Address address;

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
        in.readList(this,null);
        geoHash = GeoHash.CREATOR.createFromParcel(in);

        try {
            address = Address.CREATOR.createFromParcel(in);
        }
        catch (NullPointerException e){
            address = null;
        }
    }

    public PhotoGroup(GeoHash hash, long id){
        geoHash = hash;
        add(new HashedPhoto(id,hash.toBase32()));
        address = null;
    }

    public void append(long id,GeoHash hash){
        if(!hash.within(geoHash)) geoHash.extend(hash);
        add(new HashedPhoto(id,hash.toBase32()));
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
        if(address==null) return "";
        StringBuilder builder = new StringBuilder();
        int index = address.getMaxAddressLineIndex();
        for(int i=0;i<=index;i++) builder.append(address.getAddressLine(i)).append(" ");
        if(address.getAdminArea()!=null) builder.append(address.getLocality()).append(" ");
        if(address.getSubAdminArea()!=null) builder.append(address.getSubAdminArea()).append(" ");
        if(address.getAdminArea()!=null) builder.append(address.getAdminArea()).append(" ");
        if(address.getCountryCode()!=null) builder.append(address.getCountryCode()).append(" ");
        return builder.toString();
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(this);
        geoHash.writeToParcel(out, flags);
        if(address!=null)address.writeToParcel(out,flags);
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
