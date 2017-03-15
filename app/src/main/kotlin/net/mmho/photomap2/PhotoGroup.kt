package net.mmho.photomap2

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import net.mmho.photomap2.geohash.GeoHash
import java.io.IOException
import java.util.*

class PhotoGroup : ArrayList<HashedPhoto>, Parcelable {
    var marker: Marker? = null
    var hash: GeoHash
        private set
    var title: String? = null
        private set
    var description = ""
        private set

    var date_taken:Long = 0
        private set


    constructor(src: Parcel) {
        src.readTypedList(this, HashedPhoto.CREATOR)
        hash = GeoHash.CREATOR.createFromParcel(src)
        title = src.readString()
        description = src.readString()
        date_taken = src.readLong()

    }

    constructor(p: HashedPhoto) {
        hash = p.hash
        date_taken = p.date_taken
        add(p)
    }

    fun append(o: PhotoGroup): PhotoGroup {
        if (!o.hash.within(hash)) hash = hash.extend(o.hash)
        for (p in o) {
            add(p)
        }
        return this
    }

    fun resolveAddress(context: Context):Boolean{
        if(NetworkUtils.networkCheck(context)){
            try {
                val addresses: List<Address>? =
                    Geocoder(context).getFromLocation(center.latitude, center.longitude, 1)
                when (addresses?.size) {
                    1 -> {
                        title = addresses.first().getTitle(context)
                        return true
                    }
                }
            }
            catch (_: IOException){
            }
        }
        return false
    }

    val center: LatLng
        get() = hash.center

    override fun toString(): String = description

    override fun describeContents(): Int = 0

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeTypedList(this)
        hash.writeToParcel(out, flags)
        out.writeString(title)
        out.writeString(description)
        out.writeLong(date_taken)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<PhotoGroup> {

            override fun createFromParcel(`in`: Parcel): PhotoGroup {
                return PhotoGroup(`in`)
            }

            override fun newArray(size: Int): Array<PhotoGroup?> {
                return arrayOfNulls(size)
            }
        }

        fun getMarkerColor(size: Int): Float {
            return when {
                size >= 100 -> {
                    BitmapDescriptorFactory.HUE_RED
                }
                size >= 10 -> {
                    BitmapDescriptorFactory.HUE_ROSE
                }
                size > 1 -> {
                    BitmapDescriptorFactory.HUE_ORANGE
                }
                else -> {
                    BitmapDescriptorFactory.HUE_GREEN
                }
            }
        }
    }
}
