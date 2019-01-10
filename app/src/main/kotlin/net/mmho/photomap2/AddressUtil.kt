package net.mmho.photomap2

import android.content.Context
import android.location.Address
import com.google.android.gms.maps.model.LatLng

fun Address.getTitle(c:Context):String{
    return buildString {
        val sep = c.getString(R.string.address_separator)
        if(adminArea!=null) append(adminArea,sep)
        if(subAdminArea!=null) append(subAdminArea,sep)
        if(locality!=null) append(locality)
        if(length==0) append(featureName)
    }
}

fun Address.getDescription() : String {

    return buildString {
        val lines = maxAddressLineIndex

        repeat(lines){
            append(getAddressLine(it))
        }
    }
}

fun Address.toLatLng():LatLng{
    return LatLng(latitude,longitude)
}