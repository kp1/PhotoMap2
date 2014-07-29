package net.mmho.photomap2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class PhotoImageLoader extends AsyncTaskLoader<Bitmap> {

    private static final String TAG = "PhotoImageLoader";
    private long image_id;
    private Context context;
    private Bitmap bitmap;
    private int width;

    public PhotoImageLoader(Context context, long image_id,int width) {
        super(context);
        this.image_id = image_id;
        this.context = context;
        this.width = width;
        onContentChanged();
    }

    @Override
    public Bitmap loadInBackground() {

        final String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.ORIENTATION,
                MediaStore.Images.ImageColumns.DATA,
        };
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor c = MediaStore.Images.Media.query(context.getContentResolver(), uri, projection,
                QueryBuilder.createQuery(image_id), null, null);

        Bitmap bmp = null;

        if (c.getColumnCount() > 0) {
            c.moveToFirst();
            String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

            BitmapFactory.Options option = new BitmapFactory.Options();

            // get only size
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path,option);

            int orientation = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));

            int s = Math.max(option.outHeight,option.outWidth) /width+1;
            int scale = 1;
            while (scale < s) scale *= 2;

            option.inSampleSize = scale;
            option.inJustDecodeBounds = false;
            bmp = BitmapFactory.decodeFile(path, option);
            if (orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(orientation);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
            }
        }
        c.close();
        return bmp;
    }

    @Override
    public void deliverResult(Bitmap data) {
        bitmap = data;
        if(isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
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
    protected void onReset() {
        super.onReset();
        onStopLoading();
        bitmap = null;
    }
}
