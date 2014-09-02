package net.mmho.photomap2;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

public class PhotoImageLoader extends AsyncTaskLoader<Bitmap> {

    private long image_id;
    private int width;
    private boolean thumbnail;

    public PhotoImageLoader(Context context, long image_id,int width,boolean thumbnail) {
        super(context);
        this.image_id = image_id;
        this.width = width;
        this.thumbnail = thumbnail;
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
        Cursor c = MediaStore.Images.Media.query(getContext().getContentResolver(), uri, projection,
                QueryBuilder.createQuery(image_id), null, null);

        Bitmap bmp = null;

        if(thumbnail){
            bmp = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(),image_id,
                    MediaStore.Images.Thumbnails.MINI_KIND,null);
        }

        if (c.getCount() > 0) {
            c.moveToFirst();
            int orientation = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));

            if(!thumbnail) {
                BitmapFactory.Options option = new BitmapFactory.Options();
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                // get only size
                option.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, option);


                int s = Math.max(option.outHeight, option.outWidth) / width + 1;
                int scale = 1;
                while (scale < s) scale *= 2;

                option.inSampleSize = scale;
                option.inJustDecodeBounds = false;
                option.inPreferredConfig = Bitmap.Config.RGB_565;
                bmp = BitmapFactory.decodeFile(path, option);
            }

            if (bmp!=null && orientation != 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(orientation);
                Bitmap oldBmp = bmp;
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
                oldBmp.recycle();
            }
        }
        c.close();
        return bmp;
    }

    @Override
    public void deliverResult(Bitmap data) {
        if(isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if(takeContentChanged()){
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
    }
}
