package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class GeocodeLoader extends AsyncTaskLoader<Integer> {

    private static final int ADDRESS_MAX_RESULTS = 3;
    private PhotoGroupList list;

    public GeocodeLoader(Context context,PhotoGroupList list) {
        super(context);
        this.list = list;
        onContentChanged();
    }

    @Override
    public Integer loadInBackground() {
        Geocoder g = new Geocoder(getContext());
        int success = 0;
        for(PhotoGroup group:list) {
            LatLng location = group.getCenter();
            List<Address> addresses;
            try {
                addresses = g.getFromLocation(location.latitude, location.longitude, ADDRESS_MAX_RESULTS);
                if(addresses!=null && addresses.size()>0){
                    group.address = addresses.get(0);
                    success++;
                }
            } catch (IOException e) {
                // do nothing
            }
        }
        return success;
    }

    @Override
    protected void onStartLoading() {
        if(takeContentChanged()){
            forceLoad();
        }
    }
}
