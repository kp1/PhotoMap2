package net.mmho.photomap2

import android.database.Cursor
import android.database.CursorWrapper
import android.provider.MediaStore.Images.Media.*
import com.google.android.gms.maps.model.LatLng
import net.mmho.photomap2.geohash.GeoHash
import java.util.*

internal class PhotoCursor(cursor: Cursor) : CursorWrapper(cursor) {

    val id: Long
        get() = getLong(getColumnIndexOrThrow(_ID))

    val location: LatLng
        get() {
            val latitude = getFloat(getColumnIndexOrThrow(LATITUDE))
            val longitude = getFloat(getColumnIndexOrThrow(LONGITUDE))
            return LatLng(latitude.toDouble(), longitude.toDouble())
        }

    private val dateTaken:Long
        get() = getLong(getColumnIndexOrThrow(DATE_TAKEN))

    private fun getGeoHash(character: Int): GeoHash =
        GeoHash.createWithCharacterCount(location, character)

    private fun getHashedPhoto(character: Int): HashedPhoto =
        HashedPhoto(id, dateTaken, getGeoHash(character))

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
        val projection = arrayOf(_ID, DATA, DISPLAY_NAME, LATITUDE, LONGITUDE, DATE_TAKEN, ORIENTATION)
        private val HASH_CHARACTER_LENGTH = 9
    }

}
