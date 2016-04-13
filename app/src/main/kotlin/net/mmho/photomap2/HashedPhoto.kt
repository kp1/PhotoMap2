package net.mmho.photomap2

import android.os.Parcel
import android.os.Parcelable

import net.mmho.photomap2.geohash.GeoHash

class HashedPhoto : Parcelable {
    val photoId: Long
    val hash: GeoHash

    constructor(id: Long, hash: GeoHash) : super() {
        this.photoId = id
        this.hash = hash
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.photoId)
        hash.writeToParcel(dest, flags)
    }

    private constructor(`in`: Parcel) {
        this.photoId = `in`.readLong()
        this.hash = GeoHash.CREATOR.createFromParcel(`in`)
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
