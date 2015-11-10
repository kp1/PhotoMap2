package net.mmho.photomap2;

import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

class QueryBuilder {
    private final static String LATITUDE = MediaStore.Images.ImageColumns.LATITUDE;
    private final static String LONGITUDE = MediaStore.Images.ImageColumns.LONGITUDE;
    private final static String DATE_TAKEN = MediaStore.Images.ImageColumns.DATE_TAKEN;
    private final static String IMAGE_ID = MediaStore.Images.ImageColumns._ID;
    static String createQuery(LatLngBounds bounds){
        LatLng start = bounds.southwest;
        LatLng end = bounds.northeast;
        String latitude = String.format("%s between %s and %s and ",
                LATITUDE,Double.toString(start.latitude),Double.toString(end.latitude));
        String longitude;
        if(start.longitude<end.longitude){
            longitude= String.format("%s between %s and %s",
                    LONGITUDE,Double.toString(start.longitude),Double.toString(end.longitude));
        }
        else{
            longitude = String.format("(%s between -180.0 and %s or %s between %s and 180.0)",
                    LONGITUDE,Double.toString(end.longitude),LONGITUDE,Double.toString(start.longitude));

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
