package net.mmho.photomap2

import android.os.Parcel
import android.os.Parcelable

import net.mmho.photomap2.geohash.GeoHash

data class HashedPhoto(val photo_id:Long, val hash:GeoHash) : Parcelable {

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.photo_id)
        hash.writeToParcel(dest, flags)
    }

    private constructor(input: Parcel):
        this(input.readLong(),GeoHash.CREATOR.createFromParcel(input)) {
    }

    companion object {

        val CREATOR: Parcelable.Creator<HashedPhoto> = object : Parcelable.Creator<HashedPhoto> {
            override fun createFromParcel(source: Parcel): HashedPhoto {
                return HashedPhoto(source)
            }

            override fun newArray(size: Int): Array<HashedPhoto?> {
                return arrayOfNulls(size)
            }
        }
    }

}
