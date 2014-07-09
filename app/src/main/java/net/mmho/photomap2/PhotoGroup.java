package net.mmho.photomap2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;

public class PhotoGroup extends ArrayList<Long> {
    private LatLngBounds area;
    PhotoGroup(LatLng p,long id){
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        b.include(p);
        area = b.build();
        this.add(id);
    }

    LatLng getCenter(){
        return area.getCenter();
    }

    void append(LatLng point,long id){
        area = area.including(point);
        this.add(id);
    }
}
