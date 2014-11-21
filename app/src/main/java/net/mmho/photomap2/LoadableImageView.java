package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.widget.ImageView;

public class LoadableImageView extends ImageView{

    private static final java.lang.String EXTRA_ID = "image_id";
    private static final String EXTRA_IMAGE_WIDTH = "width";
    private long image_id = -1;
    private LoaderManager manager;
    private LruCache<Long,Bitmap> mBitmapCache;
    protected boolean thumbnail = false;

    public LoadableImageView(Context context) {
        super(context);
    }

    public LoadableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void startLoading(final LoaderManager manager, final int loader_id, final long image_id,
                             LruCache<Long,Bitmap> cache/*, final boolean thumbnail*/){


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
            post(new Runnable() {
                @Override
                public void run() {
                    Bundle b = new Bundle();
                    b.putLong(EXTRA_ID, image_id);
                    if(thumbnail) b.putInt(EXTRA_IMAGE_WIDTH,0);
                    else b.putInt(EXTRA_IMAGE_WIDTH, Math.min(getWidth(), getHeight()));
                    manager.restartLoader(loader_id, b, LoadableImageView.this.loaderCallbacks);
                }
            });
        }
    }

    LoaderManager.LoaderCallbacks<Bitmap> loaderCallbacks=
            new LoaderManager.LoaderCallbacks<Bitmap>() {
                @Override
                public Loader<Bitmap> onCreateLoader(int id, Bundle bundle) {
                    long image_id = bundle.getLong(EXTRA_ID);
                    int width = bundle.getInt(EXTRA_IMAGE_WIDTH);
                    return new PhotoImageLoader(getContext(),image_id,width);
                }

                @Override
                public void onLoadFinished(Loader<Bitmap> loader, Bitmap bitmap) {
                    long id = ((PhotoImageLoader)loader).getImageId();
                    if(image_id==id) {
                        setImageBitmap(bitmap);
                        if (mBitmapCache != null && bitmap != null) mBitmapCache.put(id, bitmap);
                        manager.destroyLoader(loader.getId());
                    }
                }

                @Override
                public void onLoaderReset(Loader<Bitmap> loader) {

                }
            };

}
