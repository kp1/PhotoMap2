package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import net.mmho.photomap2.geohash.GeoHash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public PhotoGroup append(PhotoGroup o){
        if(!o.getHash().within(geoHash)) geoHash = geoHash.extend(o.geoHash);
        for(HashedPhoto p:o){
            add(p);
        }
        return this;
    }

    private void setAddress(String title, String description){
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
        return String.format(Locale.getDefault(),"% 8.5f , % 8.5f", p.latitude, p.longitude);
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

    public PhotoGroup resolveAddress(Context context){
        Geocoder geocoder = new Geocoder(context);
        AddressRecord record = AddressRecord.getAddressByHash(getHash());
        if(record!=null){
            setAddress(record.getTitle(), record.getDescription());
            return this;
        }
        LatLng p = getCenter();
        List<Address> addresses;
        if(NetworkUtils.networkCheck(context)) {
            try {
                addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address a = addresses.get(0);
                    setAddress(AddressUtil.getTitle(a, context), AddressUtil.getDescription(a));
                    new AddressRecord(AddressUtil.getTitle(a, context), AddressUtil.getDescription(a), getHash()).save();
                }
            } catch (IOException e) {
                // do nothing
            }
        }
        return this;
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
