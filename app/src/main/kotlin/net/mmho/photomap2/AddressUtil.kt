package net.mmho.photomap2

import android.content.Context
import android.location.Address
import com.google.android.gms.maps.model.LatLng

private fun removePostalCode(source: String): String {
    return source.replaceFirst("〒[0-9¥-]*".toRegex(), "")
}

fun Address.getTitle(c:Context):String{
    return buildString {
        val sep = c.getString(R.string.address_separator)
        if(this@getTitle.adminArea!=null) append(this@getTitle.adminArea,sep)
        if(this@getTitle.subAdminArea!=null) append(this@getTitle.subAdminArea,sep)
        if(this@getTitle.locality!=null) append(this@getTitle.locality)
        if(length==0) append(this@getTitle.featureName)
    }
}

fun Address.getDescription() : String {

    return removePostalCode(buildString {
        when (this@getDescription.maxAddressLineIndex) {
            0 -> {
                append(this@getDescription.getAddressLine(0))
            }
            else -> {
                var i = 1
                do {
                    val line = this@getDescription.getAddressLine(i)
                    append(line)
                    i++
                } while (line != null)
            }
        }
    });
}

fun Address.toLatLng():LatLng{
    return LatLng(latitude,longitude)
}