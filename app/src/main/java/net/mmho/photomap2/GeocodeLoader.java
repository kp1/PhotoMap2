package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class GeocodeLoader extends AsyncTaskLoader<Integer> {

    private static final int ADDRESS_MAX_RESULTS = 3;
    private PhotoGroupList list;
    private Handler handler;

    public GeocodeLoader(Context context,PhotoGroupList list,Handler handler) {
        super(context);
        this.list = list;
        this.handler = handler;
        onContentChanged();
    }

    @Override
    public Integer loadInBackground() {
        Geocoder g = new Geocoder(getContext());
        int success = 0;
        for(PhotoGroup group:list) {
            if(isReset()) break;
            LatLng location = group.getCenter();
            List<Address> addresses;
            try {
                addresses = g.getFromLocation(location.latitude, location.longitude, ADDRESS_MAX_RESULTS);
                if(addresses!=null && addresses.size()>0){
                    group.address = addresses.get(0);
                    success++;
                    if(handler!=null){
                        handler.sendEmptyMessage(0);
                    }
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

    @Override
    protected void onReset() {
        super.onReset();

    }
}
