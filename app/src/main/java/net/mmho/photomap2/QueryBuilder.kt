package net.mmho.photomap2

import android.provider.BaseColumns
import android.provider.MediaStore.Images.ImageColumns.*
import com.google.android.gms.maps.model.LatLngBounds

internal object QueryBuilder {
    fun createQuery(bounds: LatLngBounds): String {
        val start = bounds.southwest
        val end = bounds.northeast
        val latitude = String.format("%s between %s and %s and ",
            LATITUDE, Double.toString(), java.lang.Double.toString(end.latitude))
        val longitude: String
        if (start.longitude < end.longitude) {
            longitude = String.format("%s between %s and %s",
                LONGITUDE, java.lang.Double.toString(start.longitude), java.lang.Double.toString(end.longitude))
        } else {
            longitude = String.format("(%s between -180.0 and %s or %s between %s and 180.0)",
                LONGITUDE, java.lang.Double.toString(end.longitude), LONGITUDE, java.lang.Double.toString(start.longitude))

        }

        return latitude + longitude

    }

    fun createQuery(id: Long): String {
        return BaseColumns._ID + " is " + id
    }

    fun createQuery(): String {
        return "$LATITUDE not null and $LONGITUDE not null"
    }

    fun createQueryNoLocation(): String {
        return "$LATITUDE is null or $LONGITUDE is null"
    }

    fun sortDateNewest(): String {
        return "$DATE_TAKEN desc"
    }

    fun sortDateOldest(): String {
        return "$DATE_TAKEN asc"
    }
}
