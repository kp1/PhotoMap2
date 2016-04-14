package net.mmho.photomap2

import android.content.Context
import android.location.Address
import com.google.android.gms.maps.model.LatLng

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

    return buildString {
        var i = 0
        do{
            val line = this@getDescription.getAddressLine(i++)
            append(line?:""," ")
        }while(line!=null)
    }
}

fun Address.toLatLng():LatLng{
    return LatLng(latitude,longitude)
}