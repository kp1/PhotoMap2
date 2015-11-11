package net.mmho.photomap2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import static android.provider.MediaStore.Images.ImageColumns.LATITUDE;
import static android.provider.MediaStore.Images.ImageColumns.LONGITUDE;
import static android.provider.MediaStore.Images.ImageColumns.DATE_TAKEN;
import static android.provider.MediaStore.Images.ImageColumns._ID;

class QueryBuilder {
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
        return _ID+" is "+id;
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
