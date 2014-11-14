package net.mmho.photomap2;

import ch.hsr.geohash.GeoHash;

public class GeoHashUtils {

    public static GeoHash expand(GeoHash base,GeoHash ext){
        int insignificantBits = 64-base.significantBits();
        long bits = (base.longValue() ^ext.longValue())&(~0<<insignificantBits);
        int shift=0;
        while((bits>>>shift)!=0){
            shift++;
        }
        GeoHash expand = GeoHash.fromLongValue(base.longValue(),64-Math.max(shift,insignificantBits));
        return expand;
    }

}
