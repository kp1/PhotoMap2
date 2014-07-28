package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class PhotoImageView extends ImageView{

    private static final String TAG = "PhotoImageView";
    private static final String EXTRA_IMAGE = "image";
    private long image_id;

    public PhotoImageView(Context context) {
        super(context);
    }

    public PhotoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    public void startLoading(LoaderManager manager,int loader_id,long image_id){
        if(this.image_id!=image_id){
            this.image_id = image_id;
            Bundle b = new Bundle();
            b.putLong(EXTRA_IMAGE,image_id);
            manager.destroyLoader(loader_id);
            manager.restartLoader(loader_id,b,loaderCallbacks);
        }
        else{
            if(BuildConfig.DEBUG) Log.d(TAG, "image #"+image_id+" is already loading.");
        }
    }
    LoaderManager.LoaderCallbacks<Bitmap> loaderCallbacks =
        new LoaderManager.LoaderCallbacks<Bitmap>() {
            @Override
            public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
                return new PhotoImageLoader(getContext(),args.getLong(EXTRA_IMAGE));
            }

            @Override
            public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
                setImageBitmap(data);
            }

            @Override
            public void onLoaderReset(Loader<Bitmap> loader) {

            }
        };
}
