package net.mmho.photomap2

import android.content.Context
import android.location.Address

import com.google.android.gms.maps.model.LatLng

internal object AddressUtil {
    fun getTitle(address: Address, context: Context): String {
        val builder = StringBuilder()
        val separator = context.getString(R.string.address_separator)
        // TODO: change address order with Language setting.

        if (address.adminArea != null) {
            builder.append(address.adminArea)
            builder.append(separator)
        }
        if (address.subAdminArea != null) {
            builder.append(address.subAdminArea)
            builder.append(separator)
        }
        if (address.locality != null) {
            builder.append(address.locality)
        }
        if (builder.length == 0) {
            builder.append(address.featureName)
        }

        return String(builder)
    }

    private fun removePostalCode(source: String): String {
        return source.replaceFirst("〒[0-9¥-]*".toRegex(), "")
    }

    fun getDescription(address: Address): String {
        val description = StringBuilder()
        if (address.maxAddressLineIndex == 0) {
            description.append(address.getAddressLine(0))
        } else {
            var i = 1
            val l = address.maxAddressLineIndex
            while (i <= l) {
                description.append(address.getAddressLine(i)).append(" ")
                i++
            }
        }
        return removePostalCode(description.toString())
    }

    fun addressToLatLng(address: Address): LatLng {
        return LatLng(address.latitude, address.longitude)
    }


}
