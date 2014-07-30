package net.mmho.photomap2;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class PhotoCardLayout extends RelativeLayout{

    private static final String TAG = "PhotoCardLayout";
    public static final String EXTRA_LOCATION = "location";

    private ThumbnailImageView thumbnail;
    private TextView title;
    private TextView description;
    private TextView count;
    private ImageView menu;

    private PhotoGroup group = null;

    public PhotoCardLayout(Context context) {
        super(context);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        thumbnail = (ThumbnailImageView)findViewById(R.id.thumbnail);
        title = (TextView)findViewById(R.id.title);
        description = (TextView)findViewById(R.id.description);
        menu = (ImageView)findViewById(R.id.overflow);
        menu.setOnClickListener(onClickListener);
        count = (TextView)findViewById(R.id.count);
    }

    OnClickListener onClickListener =
        new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),PhotoMapActivity.class);
                i.putExtra(PhotoMapActivity.EXTRA_GROUP,group);
                getContext().startActivity(i);
            }
        };

    public void setData(PhotoGroup g, int loader_id, LoaderManager manager){
        if(group==null || !group.equals(g)) {
            group = g;

            count.setText(String.format("%2d",g.size()));

            thumbnail.startLoading(manager,loader_id*2,g.getID(0));

            Bundle b = new Bundle();
            description.setText(g.toString());
            b.putParcelable(EXTRA_LOCATION, g.getCenter());
            manager.destroyLoader(loader_id*2+1);
            manager.restartLoader(loader_id*2+1, b,this.loaderCallbacks);
        }
        else{
            if(BuildConfig.DEBUG) Log.d(TAG,"already loading.");
        }
    }

    private LoaderManager.LoaderCallbacks<List<Address>> loaderCallbacks
            = new LoaderManager.LoaderCallbacks<List<Address>>() {

        @Override
        public Loader<List<Address>> onCreateLoader(int id, Bundle args) {
            return new GeocodeLoader(getContext(), (LatLng) args.getParcelable(EXTRA_LOCATION));
        }

        @Override
        public void onLoadFinished(Loader<List<Address>> loader, List<Address> data) {
            Address address;
            if(data!=null && data.size()>0) {
                address = data.get(0);
                if (BuildConfig.DEBUG) Log.d(TAG, "count:" + data.size());
                for (Address a : data) {
                    if (BuildConfig.DEBUG) Log.d(TAG, a.toString());
                }
                StringBuilder builder = new StringBuilder();
                if (address.getMaxAddressLineIndex() > 0) {
                    builder.append(address.getAddressLine(1));
                } else {
                    builder.append(address.getAddressLine(0));

                }
                description.setText(new String(builder));
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Address>> loader) {

        }
    };

}
