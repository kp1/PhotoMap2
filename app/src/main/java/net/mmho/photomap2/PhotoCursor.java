package net.mmho.photomap2;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;

import ch.hsr.geohash.GeoHash;

public class PhotoCursor extends CursorWrapper{

    final public static String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION,
    };

    public PhotoCursor(Cursor cursor) {
        super(cursor);
    }

    long getID(){
        return getLong(getColumnIndexOrThrow(MediaStore.Images.Media._ID));
    }

    public LatLng getLocation(){
        float latitude = getFloat(getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE));
        float longitude = getFloat(getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE));
        return new LatLng(latitude,longitude);
    }

    public GeoHash getGeoHash(int character){
        float latitude = getFloat(getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE));
        float longitude = getFloat(getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE));
        return GeoHash.withCharacterPrecision(latitude,longitude,character);

    }

}
