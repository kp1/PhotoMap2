package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class ThumbnailImageView extends ImageView{

    private static final String TAG = "ThumbnailImageView";

    public ThumbnailImageView(Context context) {
        super(context);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    LoaderCallbacks callbacks =
        new LoaderCallbacks() {
            @Override
            public void GeocodeCallback(List<Address> data) {

            }

            @Override
            public void ThumbnailCallback(Bitmap data) {
                Log.d(TAG,"bitmap is "+data.toString());
                setImageBitmap(data);
            }
        };

    public void startLoading(LoaderManager manager,int id,long image_id){
        Bundle b = new Bundle();
        b.putLong(ThumbnailCallbacks.EXTRA_ID, image_id);
        manager.initLoader(id,b,new ThumbnailCallbacks(getContext(),callbacks));
    }
}
