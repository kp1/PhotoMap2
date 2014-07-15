package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ThumbnailImageView extends ImageView implements LoaderManager.LoaderCallbacks<Bitmap>{

    private Context context;

    public ThumbnailImageView(Context context) {
        super(context);
        this.context = context;
    }

    public ThumbnailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public ThumbnailImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        return new ThumbnailLoader(context,args.getLong("test"));
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        setImageBitmap(data);

    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
    }

}
