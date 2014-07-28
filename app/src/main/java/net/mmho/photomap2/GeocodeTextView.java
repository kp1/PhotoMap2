package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class GeocodeTextView extends TextView implements LoaderManager.LoaderCallbacks<List<Address>> {
    public static final String EXTRA_LOCATION = "location";
    private static final String TAG = "GeocodeTextView";

    public GeocodeTextView(Context context) {
        super(context);
    }

    public GeocodeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GeocodeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public Loader<List<Address>> onCreateLoader(int id, Bundle args) {
        return new GeocodeLoader(getContext(), (LatLng) args.getParcelable(EXTRA_LOCATION));
    }

    @Override
    public void onLoadFinished(Loader<List<Address>> loader, List<Address> data) {
        Address address = data.get(0);
        if(BuildConfig.DEBUG) Log.d(TAG,"count:"+data.size());
        for(Address a:data){
            if(BuildConfig.DEBUG) Log.d(TAG,a.toString());
        }
        StringBuilder builder = new StringBuilder();
        if(address.getMaxAddressLineIndex()>0){
            builder.append(address.getAddressLine(1));
        }
        else{
            builder.append(address.getAddressLine(0));

        }
        setText(new String(builder));

    }

    @Override
    public void onLoaderReset(Loader<List<Address>> loader) {

    }
}
