package net.mmho.photomap2;

import android.os.Parcel;
import android.os.Parcelable;

public class HashedPhoto implements Parcelable {
    private long id;
    private String hash;

    public HashedPhoto(long id, String hash){
        this.id = id;
        this.hash = hash;
    }

    public long getId(){
        return id;
    }

    public String getHash(){
        return hash;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(hash);
    }

    private HashedPhoto(Parcel in) {
        id = in.readLong();
        hash = in.readString();
    }

    public static final Parcelable.Creator<HashedPhoto> CREATOR = new Parcelable.Creator<HashedPhoto>() {
        public HashedPhoto createFromParcel(Parcel source) {
            return new HashedPhoto(source);
        }

        public HashedPhoto[] newArray(int size) {
            return new HashedPhoto[size];
        }
    };
}
