package net.mmho.photomap2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.widget.ImageView;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class LoadableImageView extends ImageView{

    private static final String TAG = "LoadableImageView";
    protected boolean thumbnail = false;
    private int width;

    public LoadableImageView(Context context) {
        super(context);
        subscribeSubject();
    }

    public LoadableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        subscribeSubject();
    }

    public LoadableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        subscribeSubject();
    }

    PublishSubject<Long> subject;
    Subscription subscription;
    private void subscribeSubject(){
        subject = PublishSubject.create();
        subscription =
        subject
            .onBackpressureLatest()
            .subscribeOn(Schedulers.newThread())
            .switchMap(id-> loadImage(id).subscribeOn(Schedulers.newThread()))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::setImageBitmap);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscription.unsubscribe();
    }

    public void startLoading(final long image_id){

        Bitmap bitmap = null;
        if(thumbnail) bitmap = ThumbnailCache.getInstance().get(image_id);
        if(bitmap!=null){
            setImageBitmap(bitmap);
            return;
        }

        setImageDrawable(null);

        post(() -> {
            width = Math.min(getWidth(), getHeight());
            subject.onNext(image_id);
        });
    }

    private Observable<Bitmap> loadImage(Long image_id){
        return Observable.create( subscriber -> {
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
            if(thumbnail&&bmp!=null) ThumbnailCache.getInstance().put(image_id,bmp);
            subscriber.onNext(bmp);
            subscriber.onCompleted();
            c.close();
        });
    }
}
