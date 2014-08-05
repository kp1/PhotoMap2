package net.mmho.photomap2;

import android.content.Context;
import android.widget.ArrayAdapter;

public class DistanceAdapter extends ArrayAdapter<CharSequence>{

    private final static String[] label={
        "100m",
        "1km",
        "10km",
        "100km",
        "1,000km",
        "10,000km",
    };

    private static final float[] distance={
            50,
            500,
            5*1000,
            50*1000,
            500*1000,
            5000*1000,
    };

    public DistanceAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return label.length;
    }

    @Override
    public CharSequence getItem(int position) {
        return String.format("%8s",label[position]);
    }

    static public float getDistance(int position){
        return distance[position];
    }

    static public int initial(){
        return 1;
    }

}
