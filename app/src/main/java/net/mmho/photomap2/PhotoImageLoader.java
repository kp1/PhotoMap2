package net.mmho.photomap2;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PhotoImageLoader extends AsyncTaskLoader<Bitmap> {

    private static final String TAG = "PhotoImageLoader";
    private long image_id;
    private Context context;

    public PhotoImageLoader(Context context, long image_id) {
        super(context);
        this.image_id = image_id;
        this.context = context;
        onContentChanged();
    }

    @Override
    public Bitmap loadInBackground() {
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,image_id);
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Rect rect = new Rect();
            options.inSampleSize = 4;

            Bitmap bmp = BitmapFactory.decodeStream(is,rect,options);
            is.close();

            return bmp;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        if(takeContentChanged()){
            forceLoad();
        }
    }
}
