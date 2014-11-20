package net.mmho.photomap2;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import net.mmho.photomap2.geohash.GeoHash;

public class UpdateDBService extends Service implements Loader.OnLoadCompleteListener<Cursor> {

    CursorLoader mCursorLoader;
    private final static int IMAGE_LOADER_ID = 0;
    private String TAG = "UpdateDBService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onLoadCreate");
        final String projection[]={
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.DATE_TAKEN,
        };
        mCursorLoader = new CursorLoader(getApplicationContext(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,QueryBuilder.createQuery(),null,null);
        mCursorLoader.registerListener(IMAGE_LOADER_ID,this);
        mCursorLoader.startLoading();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG,"onLoadComplete");
        cursor.moveToFirst();
        do{
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            HashedPhoto photo = HashedPhoto.getByPhotoId(id);
            if(photo==null){
                float latitude = cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE));
                float longitude = cursor.getFloat(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE));
                photo = new HashedPhoto(id, GeoHash.create(latitude,longitude,9*5));
                photo.save();
            }
        }while(cursor.moveToNext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        if(mCursorLoader!=null){
            mCursorLoader.unregisterListener(this);
            mCursorLoader.cancelLoad();
            mCursorLoader.stopLoading();
        }
    }
}
