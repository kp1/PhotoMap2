package net.mmho.photomap2

import android.os.Parcel
import android.os.Parcelable

import net.mmho.photomap2.geohash.GeoHash

data class HashedPhoto(val photo_id:Long,val date_taken: Long,val hash:GeoHash) : Parcelable {
    constructor(source: Parcel):
        this(source.readLong(),
             source.readLong(),
             source.readParcelable<GeoHash>(GeoHash::class.java.classLoader)!!)

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run{
            writeLong(photo_id)
            writeLong(date_taken)
            writeParcelable(hash, 0)
        }
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<HashedPhoto> = object : Parcelable.Creator<HashedPhoto> {
            override fun createFromParcel(source: Parcel): HashedPhoto = HashedPhoto(source)
            override fun newArray(size: Int): Array<HashedPhoto?> = arrayOfNulls(size)
        }
    }
}
