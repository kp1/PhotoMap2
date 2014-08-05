package net.mmho.photomap2;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;

public class PhotoCursor{

    final public static String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION,
    };

    static long getID(Cursor c){
        return c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
    }

    static LatLng getLocation(Cursor c){
        float latitude = c.getFloat(c.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE));
        float longitude = c.getFloat(c.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE));
        return new LatLng(latitude,longitude);
    }

}
