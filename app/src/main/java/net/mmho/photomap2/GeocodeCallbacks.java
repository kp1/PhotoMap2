package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.location.Address;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class GeocodeCallbacks implements LoaderManager.LoaderCallbacks<List<Address>>{

    public static final String EXTRA_LOCATION = "location";
    private LoaderCallbacks callback;
    private Context context;

    GeocodeCallbacks(Context context, LoaderCallbacks callback){
        this.callback = callback;
        this.context = context;
    }

    @Override
    public Loader<List<Address>> onCreateLoader(int id, Bundle args) {
        return new GeocodeLoader(context, (LatLng) args.getParcelable(EXTRA_LOCATION));
    }

    @Override
    public void onLoadFinished(Loader<List<Address>> loader, List<Address> data) {
        callback.GeocodeCallback(data);

    }

    @Override
    public void onLoaderReset(Loader<List<Address>> loader) {

    }
}
