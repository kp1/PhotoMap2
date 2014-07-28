package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

public class ThumbnailLoader extends AsyncTaskLoader<Bitmap> {

    private long id;
    private Bitmap bitmap;

    public ThumbnailLoader(Context c,long id) {
        super(c);
        this.id = id;
        onContentChanged();
    }

    @Override
    public Bitmap loadInBackground() {
        return MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(),id,
                MediaStore.Images.Thumbnails.MINI_KIND,null);
    }

    @Override
    public void deliverResult(Bitmap data) {
        bitmap = data;
        if(isStarted()) {
            super.deliverResult(data);
        }
    }

    protected void onStartLoading(){
        if(bitmap!=null){
            deliverResult(bitmap);
        }
        if(takeContentChanged() || bitmap==null){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(Bitmap data) {
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
        bitmap=null;
    }
}
