package net.mmho.photomap2;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import net.mmho.photomap2.geohash.GeoHash;

@Table(name="AddressTable")
public class AddressRecord extends Model{
    @Column(name="address")
    private String address;
    @Column(name="description")
    private String description;
    @Column(name="hash")
    private long hash;
    @Column(name="length")
    private int length;

    @SuppressWarnings("unused")
    public AddressRecord(){
        super();
    }

    public AddressRecord(String title,String desc,GeoHash geoHash){
        super();
        address = title;
        description = desc;
        hash = geoHash.getLong();
        length = geoHash.getSignificantBits();
    }

    public String getTitle(){
        return address;
    }

    public String getDescription(){
        return description;
    }

    public static AddressRecord getAddressByHash(GeoHash hash){
        return new Select().from(AddressRecord.class)
                .where("hash = ?", hash.getLong()).and("length = ?",hash.getSignificantBits())
                .executeSingle();
    }

    public static void clearCache(){
        new Delete().from(AddressRecord.class).execute();
    }
}
