package net.mmho.photomap2

import android.database.Cursor
import android.database.CursorWrapper
import android.provider.MediaStore

import com.google.android.gms.maps.model.LatLng

import net.mmho.photomap2.geohash.GeoHash

import java.util.ArrayList

internal class PhotoCursor(cursor: Cursor) : CursorWrapper(cursor) {

    val id: Long
        get() = getLong(getColumnIndexOrThrow(MediaStore.Images.Media._ID))

    val location: LatLng
        get() {
            val latitude = getFloat(getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE))
            val longitude = getFloat(getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE))
            return LatLng(latitude.toDouble(), longitude.toDouble())
        }

    fun getGeoHash(character: Int): GeoHash {
        return GeoHash.createWithCharacterCount(location, character)
    }

    fun getHashedPhoto(character: Int): HashedPhoto {
        return HashedPhoto(id, getGeoHash(character))
    }

    val hashedPhotoList: ArrayList<HashedPhoto>
        get() {
            val list = ArrayList<HashedPhoto>()
            if (isClosed || !moveToFirst()) return list
            do {
                list.add(getHashedPhoto(HASH_CHARACTER_LENGTH))
            } while (moveToNext())
            return list
        }

    companion object {

        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.ORIENTATION)

        private val HASH_CHARACTER_LENGTH = 9
    }

}
