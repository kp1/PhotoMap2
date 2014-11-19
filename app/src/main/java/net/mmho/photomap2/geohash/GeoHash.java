package net.mmho.photomap2.geohash;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class GeoHash implements Parcelable {

    long bit;
    int significantBits;

    private final static int MAX_SIGNIFICANT_BIT = 64;
    private final static String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    static class Divider{
        public double upper;
        public double lower;
        Divider(double abs){
            upper = abs;
            lower = -abs;
        }

        double middle(){
            return (upper+lower)/2;
        }
    }

    protected GeoHash(){

    }

    public static GeoHash createWithCharacterCount(LatLng latLng,int length){
        return create(latLng,length*5);
    }

    public static GeoHash create(LatLng latlng,int significant){
        return create(latlng.latitude,latlng.longitude,significant);
    }

    public static GeoHash createFromLong(long bit,int significant){
        GeoHash hash = new GeoHash();
        hash.bit = bit;
        hash.significantBits = significant;
        return hash;
    }

    public static GeoHash create(double latitude, final double longitude,int significant){

        if(significant>MAX_SIGNIFICANT_BIT){
            throw new IllegalArgumentException(String.format("significant bit must under %d.",MAX_SIGNIFICANT_BIT));
        }

        GeoHash hash = new GeoHash();
        hash.significantBits = significant;
        long bit = ~(~0L>>>1);       // MSB = 1;
        hash.bit = 0;

        double[] p = {longitude,latitude};
        Divider[] dividers = {new Divider(180.0),new Divider(90.0)};
        for(int i=0;i<significant;i++){
            int index = i%2;
            Divider div = dividers[index];
            double middle = div.middle();
            if(p[index]>middle){
                hash.bit |= bit;
                div.lower = middle;
            }
            else{
                div.upper = middle;
            }
            bit>>>=1;
        }
        return hash;
    }

    public long getLong(){
        return bit;
    }

    public int getSignificantBits(){
        return significantBits;
    }

    public void extend(GeoHash ext){
        int significant = Math.min(significantBits,ext.significantBits);
        long xor_bit = (bit^ext.bit)&~(~0L>>>significant);
        while(xor_bit!=0){
            significant--;
            xor_bit = (bit^ext.bit)&~(~0L>>>significant);
        }
        bit &= ~(~0L>>>significant);
        significantBits = significant;
    }

    public boolean within(GeoHash base){
        return significantBits >= base.significantBits && within(base, base.significantBits);
    }

    public boolean within(GeoHash base,int significant){
        return ((bit ^ base.bit) & (~(~0L >>> significant))) == 0;
    }

    private Divider[] getArea(){
        Divider[] dividers = {new Divider(180.0),new Divider(90.0)};
        long check_bit = ~(~0L>>>1);
        for(int i=0;i<significantBits;i++){
            Divider div = dividers[i%2];
            if((bit&check_bit)!=0){
                div.lower = div.middle();
            }
            else{
                div.upper = div.middle();
            }
            check_bit>>>=1;
        }
        return dividers;
    }

    public LatLngBounds getBounds(){
        Divider[] dividers = getArea();
        LatLng northeast = new LatLng(dividers[1].upper,dividers[0].upper);
        LatLng southwest = new LatLng(dividers[1].lower,dividers[0].lower);
        return new LatLngBounds(southwest,northeast);
    }

    public LatLng getCenter() {
        Divider[] dividers = getArea();
        return new LatLng(dividers[1].middle(),dividers[0].middle());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.bit);
        dest.writeInt(this.significantBits);
    }

    private GeoHash(Parcel in) {
        this.bit = in.readLong();
        this.significantBits = in.readInt();
    }

    public static final Parcelable.Creator<GeoHash> CREATOR = new Parcelable.Creator<GeoHash>() {
        public GeoHash createFromParcel(Parcel source) {
            return new GeoHash(source);
        }

        public GeoHash[] newArray(int size) {
            return new GeoHash[size];
        }
    };
}
