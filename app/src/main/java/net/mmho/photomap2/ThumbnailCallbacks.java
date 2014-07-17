package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;

public class ThumbnailCallbacks implements LoaderManager.LoaderCallbacks<Bitmap> {
    public final static String EXTRA_ID="thumbnail_id";
    private LoaderCallbacks callback;
    private Context context;

    ThumbnailCallbacks(Context context,LoaderCallbacks callback){
        this.callback = callback;
        this.context = context;
    }
    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new ThumbnailLoader(context,args.getLong(EXTRA_ID));
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        callback.ThumbnailCallback(data);

    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {

    }
}
