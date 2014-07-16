package net.mmho.photomap2;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

public class ThumbnailLoader extends AsyncTaskLoader<Bitmap> {

    private long id;

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

    protected void onStartLoading(){
        if(takeContentChanged()){
            forceLoad();
        }
    }

}
