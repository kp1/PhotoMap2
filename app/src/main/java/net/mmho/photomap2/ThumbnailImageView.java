package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.List;

public class ThumbnailImageView extends ImageView{

    private static final String TAG = "ThumbnailImageView";
    private long image_id = -1;

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
                setImageBitmap(data);
            }
        };

    public void startLoading(LoaderManager manager,int loader_id,long image_id){

        if(image_id!=this.image_id) {
            setImageBitmap(null);
            this.image_id = image_id;
            Bundle b = new Bundle();
            b.putLong(ThumbnailCallbacks.EXTRA_ID, image_id);
            manager.destroyLoader(loader_id);
            manager.restartLoader(loader_id, b, new ThumbnailCallbacks(getContext(), callbacks));
        }
        else{
            if(BuildConfig.DEBUG)Log.d(TAG,"image #"+image_id+" is already loading.");
        }
    }
}
