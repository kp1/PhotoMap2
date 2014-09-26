package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PhotoImageView extends ImageView{

    private static final String EXTRA_IMAGE = "image";
    private static final String EXTRA_WIDTH = "width";
    private long image_id = -1;
    private LoaderManager manager;

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

    public void startLoading(final LoaderManager manager, final int loader_id, long id){
        if(image_id!=id){
            image_id = id;
            this.manager = manager;
            post(new Runnable() {
                @Override
                public void run() {
                    Bundle b = new Bundle();
                    b.putLong(EXTRA_IMAGE, image_id);
                    b.putInt(EXTRA_WIDTH,Math.min(getWidth(),getHeight()));
                    manager.restartLoader(loader_id, b, loaderCallbacks);
                }
            });
        }
    }
    LoaderManager.LoaderCallbacks<Bitmap> loaderCallbacks =
        new LoaderManager.LoaderCallbacks<Bitmap>() {
            @Override
            public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
                return new PhotoImageLoader(getContext(),args.getLong(EXTRA_IMAGE),args.getInt(EXTRA_WIDTH),false);
            }

            @Override
            public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
                setImageBitmap(data);
                manager.destroyLoader(loader.getId());
            }

            @Override
            public void onLoaderReset(Loader<Bitmap> loader) {

            }
        };
}
