package net.mmho.photomap2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class LoadableImageView extends ImageView{

    protected boolean thumbnail = false;
    private int width;
    private long image_id = -1;

    private Observable<Bitmap> observable;

    public LoadableImageView(Context context) {
        super(context);
    }

    public LoadableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void startLoading(final long image_id,final LruCache<Long,Bitmap> cache){

        if(this.image_id==image_id){
            return;
        }
        Bitmap bmp = null;
        this.image_id = image_id;
        if(cache!=null) bmp = cache.get(image_id);
        if(bmp!=null){
            setImageBitmap(bmp);
            return;
        }

        setImageDrawable(null);

        post(new Runnable() {
            @Override
            public void run() {
                width = Math.min(getWidth(), getHeight());
                // load image
                if(observable==null){
                    observable = request();
                    ViewObservable.bindView(LoadableImageView.this,observable);
                }
                observable
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Bitmap>() {

                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Bitmap bitmap) {
                            setImageBitmap(bitmap);
                            if (cache != null) cache.put(image_id, bitmap);
                        }
                    });
            }
        });
    }

    private Observable<Bitmap> request(){
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
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
                subscriber.onNext(bmp);
                subscriber.onCompleted();
            }
        });
    }
}
