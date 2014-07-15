package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;

import java.util.List;

public class ThumbnailAdapter extends ArrayAdapter<Long>{

    private static final int ID_IMAGES= 100;
    private static final String TAG = "ThumbnailAdapter";
    private Context context;
    private int id;
    private List<Long> group;
    private LayoutInflater inflater;
    private LoaderManager manager;

    public ThumbnailAdapter(Context c, int resource, List<Long> objects,LoaderManager m) {
        super(c, resource, objects);
        context = c;
        id = resource;
        group = objects;
        manager = m;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(BuildConfig.DEBUG) Log.d(TAG, "getView:" + position);

        View v;
        ThumbnailImageView i;
        if(convertView!=null){
            v = convertView;
            i = (ThumbnailImageView)v.findViewById(R.id.thumbnail);

        }
        else{
            v = inflater.inflate(id,null);
            i = new ThumbnailImageView(context);
            i.setId(R.id.thumbnail);
                    ((FrameLayout) v.findViewById(R.id.frame)).addView(i);
        }


        int width= ((GridView)parent).getColumnWidth();
        v.setLayoutParams(new AbsListView.LayoutParams(width, width));

        if(position%2==0) i.setBackgroundColor(Color.LTGRAY);

        Bundle b = new Bundle();
        b.putLong("test",group.get(position));
        manager.initLoader(ID_IMAGES+position,b,i);

        return v;
    }
}
