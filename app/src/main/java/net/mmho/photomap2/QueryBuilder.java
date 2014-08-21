package net.mmho.photomap2;

import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class QueryBuilder {
    final static String LATITUDE = MediaStore.Images.ImageColumns.LATITUDE;
    final static String LONGITUDE = MediaStore.Images.ImageColumns.LONGITUDE;
    final static String DATE_TAKEN = MediaStore.Images.ImageColumns.DATE_TAKEN;
    final static String IMAGE_ID = MediaStore.Images.ImageColumns._ID;
    static String createQuery(LatLngBounds bounds){
        LatLng start = bounds.southwest;
        LatLng end = bounds.northeast;
        String latitude = String.format("%s between %f and %f and ",
                LATITUDE,start.latitude,end.latitude);
        String longitude;
        if(start.longitude<end.longitude){
            longitude= String.format("%s between %f and %f",
                    LONGITUDE,start.longitude,end.longitude);
        }
        else{
            longitude = String.format("(%s between -180.0 and %f or %s between %f and 180.0)",
                    LONGITUDE,end.longitude,LONGITUDE,start.longitude);

        }

        return latitude+longitude;

    }

    static String createQuery(long id){
        return IMAGE_ID+" is "+id;
    }

    static String createQuery(){
        return LATITUDE+" not null and "+LONGITUDE+" not null";
    }

    static String createQueryNoLocation(){
        return LATITUDE+" is null or "+LONGITUDE+"is null";
    }

    static String sortDateNewest(){
        return DATE_TAKEN+" desc";
    }

    static String sortDateOldest() {
        return DATE_TAKEN+" asc";
    }

}
