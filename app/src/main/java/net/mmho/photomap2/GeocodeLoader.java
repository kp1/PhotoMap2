package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class GeocodeLoader extends AsyncTaskLoader<List<Address>> {

    private static final int ADDRESS_MAX_RESULTS = 3;
    LatLng location;
    List<Address> addresses;

    public GeocodeLoader(Context context,LatLng location) {
        super(context);
        addresses = null;
        this.location = location;
        onContentChanged();
    }

    @Override
    public List<Address> loadInBackground() {
        Geocoder g = new Geocoder(getContext());

        try {
            addresses = g.getFromLocation(location.latitude,location.longitude,ADDRESS_MAX_RESULTS);
        } catch (IOException e) {
            return null;
        }
        return addresses;
    };

    @Override
    protected void onStartLoading() {
        if(takeContentChanged()){
            forceLoad();
        }
    }
}
