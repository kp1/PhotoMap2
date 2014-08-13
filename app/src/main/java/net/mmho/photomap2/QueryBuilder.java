package net.mmho.photomap2;

import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class QueryBuilder {
    final static String LATITUDE = MediaStore.Images.ImageColumns.LATITUDE;
    final static String LONGITUDE = MediaStore.Images.ImageColumns.LONGITUDE;
    final static String DATE_TAKEN = MediaStore.Images.ImageColumns.DATE_TAKEN;
    final static String IMAGE_ID = MediaStore.Images.ImageColumns._ID;
    final static String BETWEEN = " BETWEEN ";
    final static String AND = " AND ";
    final static String OR = " OR ";
    final static String IS = " IS ";
    final static String NOT = " NOT ";
    final static String NULL= " NULL ";
    static String createQuery(LatLngBounds bounds){
        LatLng start = bounds.southwest;
        LatLng end = bounds.northeast;
        String q[] = {LATITUDE,BETWEEN, String.valueOf(start.latitude),AND, String.valueOf(end.latitude),
                 AND, LONGITUDE,BETWEEN, String.valueOf(start.longitude),AND, String.valueOf(end.longitude)};
        StringBuilder b = new StringBuilder();
        for(String s:q){
            b.append(s);
        }
        return b.toString();

    }

    static String createQuery(long id){
        return IMAGE_ID+IS+id;
    }

    static String createQuery(){
        return LATITUDE+NOT+NULL+AND+LONGITUDE+NOT+NULL;
    }

    static String createQueryNoLocation(){
        return LATITUDE+IS+NULL+OR+LONGITUDE+IS+NULL;
    }

    static String sortDateNewest(){
        return DATE_TAKEN+" DESC";
    }

    static String sortDateOldest() {
        return DATE_TAKEN+" ASC";
    }

}
