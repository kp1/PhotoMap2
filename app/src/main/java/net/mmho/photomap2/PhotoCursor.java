package net.mmho.photomap2;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;

import net.mmho.photomap2.geohash.GeoHash;

import java.util.ArrayList;

class PhotoCursor extends CursorWrapper{

    final public static String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.ORIENTATION,
    };

    private static final int HASH_CHARACTER_LENGTH=9;


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
        return GeoHash.createWithCharacterCount(getLocation(),character);
    }

    public HashedPhoto getHashedPhoto(int character){
        return new HashedPhoto(getID(),getGeoHash(character));
    }

    public ArrayList<HashedPhoto> getHashedPhotoList(){
        ArrayList<HashedPhoto> list = new ArrayList<>();
        if(isClosed()||!moveToFirst()) return list;
        do{
            list.add(getHashedPhoto(HASH_CHARACTER_LENGTH));
        }while(moveToNext());
        return list;
    }

}
