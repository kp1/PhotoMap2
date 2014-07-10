package net.mmho.photomap2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class QueryBuilder {
    static String createQuery(LatLngBounds bounds){
        LatLng start = bounds.southwest;
        LatLng end = bounds.northeast;
        StringBuilder b = new StringBuilder();
        b.append("(latitude between ").append(start.latitude).append(" and ").append(end.latitude).append(")");
        b.append(" and (longitude between ").append(start.longitude).append(" and ").append(end.longitude).append(")");
        return new String(b);

    }

    static String createQuery(){
        return "( latitude not null ) and ( longitude not null )";
    }

    static String createQueryNoLocation(){
        return "( latitude is null ) or ( longitude is null )";
    }
}
