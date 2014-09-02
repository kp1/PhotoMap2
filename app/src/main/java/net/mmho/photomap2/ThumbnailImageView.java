package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.ImageView;

public class ThumbnailImageView extends ImageView{

    private static final java.lang.String EXTRA_ID = "thumbnail_id";
    private long image_id = -1;
    private LoaderManager manager;
    private LruCache<Long,Bitmap> mBitmapCache;

    public ThumbnailImageView(Context context) {
        super(context);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void startLoading(LoaderManager manager,int loader_id,long image_id,LruCache<Long,Bitmap> cache){

        if(image_id!=this.image_id) {
            this.image_id = image_id;
            this.manager = manager;
            mBitmapCache = cache;

            Bitmap bmp = null;
            if(cache!=null) bmp = cache.get(image_id);
            if(bmp!=null){
                setImageBitmap(bmp);
                return;
            }

            setImageDrawable(null);
            Bundle b = new Bundle();
            b.putLong(EXTRA_ID, image_id);
            manager.restartLoader(loader_id, b,this.loaderCallbacks);
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
                return new PhotoImageLoader(getContext(),args.getLong(EXTRA_ID),0,true);
            }

            @Override
            public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
                setImageBitmap(data);
                if(mBitmapCache!=null)mBitmapCache.put(image_id,data);
                manager.destroyLoader(loader.getId());
            }

            @Override
            public void onLoaderReset(Loader<Bitmap> loader) {

            }
        };
}
