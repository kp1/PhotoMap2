package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ThumbnailImageView extends ImageView{

    private static final String TAG = "ThumbnailImageView";
    private static final java.lang.String EXTRA_ID = "thumbnail_id";
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

    public void startLoading(LoaderManager manager,int loader_id,long image_id){

        if(image_id!=this.image_id) {
            setImageBitmap(null);
            this.image_id = image_id;
            Bundle b = new Bundle();
            b.putLong(EXTRA_ID, image_id);
            manager.destroyLoader(loader_id);
            manager.restartLoader(loader_id, b,this.loaderCallbacks);
        }
        else{
            if(BuildConfig.DEBUG)Log.d(TAG,"image #"+image_id+" is already loading.");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,widthMeasureSpec);
    }

    LoaderManager.LoaderCallbacks<Bitmap> loaderCallbacks=
        new LoaderManager.LoaderCallbacks<Bitmap>() {
            @Override
            public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
                return new ThumbnailLoader(getContext(),args.getLong(EXTRA_ID));
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
