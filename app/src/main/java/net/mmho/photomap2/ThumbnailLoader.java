package net.mmho.photomap2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

public class ThumbnailLoader extends AsyncTaskLoader<Bitmap> {

    private long id;
    private Bitmap bitmap;
    private Context context;

    public ThumbnailLoader(Context c,long id) {
        super(c);
        this.id = id;
        context = c;
        onContentChanged();
    }

    @Override
    public Bitmap loadInBackground() {
        final String[] projection = {
                MediaStore.Images.ImageColumns.ORIENTATION,
        };
        final Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(),id,
                MediaStore.Images.Thumbnails.MINI_KIND,null);

        Cursor c = MediaStore.Images.Media.query(context.getContentResolver(), uri, projection,
                QueryBuilder.createQuery(id), null, null);

        if(c.getColumnCount()>0){
            c.moveToFirst();
            int orientation = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
            if(orientation!=0){
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
