package net.mmho.photomap2;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

public class ThumbnailLoader extends AsyncTaskLoader<Bitmap> {

    private Context context;
    private long id;

    public ThumbnailLoader(Context c,long id) {
        super(c);
        context = c;
        this.id = id;
    }

    @Override
    public Bitmap loadInBackground() {
        Log.d("ThumbnailLoader", "id:" + id);
        return MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),id,
                MediaStore.Images.Thumbnails.MINI_KIND,null);
    }
}
