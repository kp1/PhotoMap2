package net.mmho.photomap2;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class PhotoGroup extends ArrayList<Long> implements Parcelable{
    public Marker marker;
    private LatLngBounds area;

    public Address address;

    public final static Parcelable.Creator<PhotoGroup> CREATOR = new Parcelable.Creator<PhotoGroup>(){

        @Override
        public PhotoGroup createFromParcel(Parcel in) {
            return new PhotoGroup(in);
        }

        @Override
        public PhotoGroup[] newArray(int size) {
            return new PhotoGroup[0];
        }
    };

    PhotoGroup(Parcel in){
        in.readList(this, null);
        area = LatLngBounds.CREATOR.createFromParcel(in);
        try {
            address = Address.CREATOR.createFromParcel(in);
        }
        catch (NullPointerException e){
            address = null;
        }
    }

    PhotoGroup(LatLng p, long id){
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(p);
        area = b.build();
        add(id);
        address = null;
    }

    public LatLng getCenter(){
        return area.getCenter();
    }
    public LatLngBounds getArea(){
        return area;
    }

    public String locationToString() {
        LatLng c = area.getCenter();
        return String.format("% 8.5f , % 8.5f", c.latitude, c.longitude);
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


    public void append(LatLng point,long id){
        area = area.including(point);
        add(id);
        address = null;
    }

    public boolean equals(PhotoGroup g) {
        return super.equals(g) && area.equals(g.area);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(this);
        area.writeToParcel(out,0);
        if(address!=null)address.writeToParcel(out,0);
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
