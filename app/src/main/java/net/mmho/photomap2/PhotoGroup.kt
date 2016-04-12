package net.mmho.photomap2

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Parcel
import android.os.Parcelable

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

import geohash.GeoHash

import java.io.IOException
import java.util.ArrayList
import java.util.Locale

class PhotoGroup : ArrayList<HashedPhoto>, Parcelable {
    var marker: Marker? = null
    var hash: GeoHash
        private set
    var title: String? = null
        private set
    var description = ""
        private set

    constructor(src: Parcel) {
        src.readTypedList(this, HashedPhoto.CREATOR)
        hash = GeoHash.CREATOR.createFromParcel(src)
        title = src.readString()
        description = src.readString()

    }

    constructor(p: HashedPhoto) {
        hash = p.hash
        add(p)
    }

    fun append(o: PhotoGroup): PhotoGroup {
        if (!o.hash.within(hash)) hash = hash.extend(o.hash)
        for (p in o) {
            add(p)
        }
        return this
    }

    private fun setAddress(title: String, description: String) {
        this.title = title
        this.description = description
    }

    val center: LatLng
        get() = hash.center

    fun locationToString(): String {
        val p = hash.center
        return String.format(Locale.getDefault(), "% 8.5f , % 8.5f", p.latitude, p.longitude)
    }

    override fun toString(): String {
        return description
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeTypedList(this)
        hash.writeToParcel(out, flags)
        out.writeString(title)
        out.writeString(description)
    }

    fun resolveAddress(context: Context): PhotoGroup {
        val geocoder = Geocoder(context)
        val p = center
        val addresses: List<Address>?
        if (NetworkUtils.networkCheck(context)) {
            try {
                addresses = geocoder.getFromLocation(p.latitude, p.longitude, 1)
                if (addresses != null && addresses.size > 0) {
                    val a = addresses[0]
                    setAddress(AddressUtil.getTitle(a, context), AddressUtil.getDescription(a))
                }
            } catch (e: IOException) {
                // do nothing
            }

        }
        return this
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
            val color: Float
            if (size >= 100) {
                color = BitmapDescriptorFactory.HUE_RED
            } else if (size >= 10) {
                color = BitmapDescriptorFactory.HUE_ROSE
            } else if (size > 1) {
                color = BitmapDescriptorFactory.HUE_ORANGE
            } else {
                color = BitmapDescriptorFactory.HUE_GREEN
            }
            return color
        }
    }
}
