package net.mmho.photomap2;

import java.text.NumberFormat;

public class DistanceUtils {

    private static final float[] distance={
            50,
            500,
            5*1000,
            50*1000,
            500*1000,
            5000*1000,
    };

    static public String pretty(int position) {
        StringBuilder pretty = new StringBuilder();
        int d = (int) (distance[position]) * 2;
        String prefix ="";
        if(d>=1000){
            d/=1000;
            prefix = "k";
        }
        pretty.append(NumberFormat.getNumberInstance().format(d)).append(prefix).append("m");
        return pretty.toString();
    }
    static public float getDistance(int position){
        return distance[position];
    }

    static public float initial(){
        return getDistance(1);
    }
    static public int size(){
        return distance.length;
    }

}
