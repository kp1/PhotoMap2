package net.mmho.photomap2;

import net.mmho.photomap2.geohash.GeoHash;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;
import ollie.query.Delete;
import ollie.query.Select;

@Table("addresses")
public class AddressRecord extends Model {
    @Column("address")
    public String address;
    @Column("description")
    public String description;
    @Column("hash")
    public String hash;

    public AddressRecord(){
        super();
    }

    public AddressRecord(String title,String desc,GeoHash geoHash){
        super();
        address = title;
        description = desc;
        hash = geoHash.getBinaryString();
    }

    public String getTitle(){
        return address;
    }

    public String getDescription(){
        return description;
    }

    public static AddressRecord getAddressByHash(GeoHash hash){
        return Select.from(AddressRecord.class)
                .where("hash = ?", hash.getBinaryString())
                .fetchSingle();
    }

    public static void clearCache(){
        Delete.from(AddressRecord.class).execute();
    }
}
