package net.mmho.photomap2;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import net.mmho.photomap2.geohash.GeoHash;

@Table(name="HashedPhoto",id=BaseColumns._ID)
public class HashedPhoto extends Model implements Parcelable {
    @Column(name="image_id")
    private long id;
    @Column(name="hash")
    private GeoHash hash;

    public HashedPhoto(long id, GeoHash hash){
        super();
        this.id = id;
        this.hash = hash;
    }

    public long getPhotoId() {
        return id;
    }

    public GeoHash getHash(){
        return hash;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        hash.writeToParcel(dest,flags);
    }

    private HashedPhoto(Parcel in) {
        this.id = in.readLong();
        GeoHash.CREATOR.createFromParcel(in);
    }

    public static final Parcelable.Creator<HashedPhoto> CREATOR = new Parcelable.Creator<HashedPhoto>() {
        public HashedPhoto createFromParcel(Parcel source) {
            return new HashedPhoto(source);
        }

        public HashedPhoto[] newArray(int size) {
            return new HashedPhoto[size];
        }
    };

    public static HashedPhoto getByPhotoId(long id){
        return new Select().from(HashedPhoto.class).where("image_id = ?",id).executeSingle();
    }
}
