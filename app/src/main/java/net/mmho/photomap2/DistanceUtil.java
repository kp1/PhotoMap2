package net.mmho.photomap2;

public class DistanceUtil {
    private static final float[] distance_table={
            10,
            1000,
            2*1000,
            4*1000,
            10*1000,
            100*1000,
            1000*1000,
            4000*4000,
    };

    static int initialIndex(){
        return 3;
    }

    static float toDistance(int index){
        return distance_table[index];
    }

    static int size(){
        return distance_table.length;
    }
}
