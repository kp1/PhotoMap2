package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.util.List;

public class GeocodeLoader extends AsyncTaskLoader<List<Address>> {

    private String location;
    List<Address> addresses;
    public GeocodeLoader(Context context,String location) {
        super(context);
        this.location = location;
        onContentChanged();
    }

    @Override
    public List<Address> loadInBackground() {
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> data = null;
        try {
            data = geocoder.getFromLocationName(location, 5);
        } catch (IOException e) {
            // do nothing.
        }
        return data;
    }

    @Override
    public void deliverResult(List<Address> data) {
        super.deliverResult(data);
        if(data!=null){
            addresses = data;
        }
    }

    @Override
    protected void onStartLoading() {
        if(addresses!=null){
            deliverResult(addresses);
        }
        if(takeContentChanged()){
            forceLoad();
        }
    }

    public String getLocation() {
        return location;
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
