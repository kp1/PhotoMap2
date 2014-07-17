package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.List;

public class ThumbnailAdapter extends ArrayAdapter<Long>{

    private static final int ID_IMAGES= 100;
    private static final String TAG = "ThumbnailAdapter";
    private int id;
    private List<Long> group;
    private LayoutInflater inflater;
    private LoaderManager manager;

    public ThumbnailAdapter(Context c, int resource, List<Long> objects,LoaderManager m) {
        super(c, resource, objects);
        id = resource;
        group = objects;
        manager = m;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    private class ViewHolder{
        ThumbnailImageView thumbnail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(BuildConfig.DEBUG) Log.d(TAG, "getView:" + position);

        View v;
        ViewHolder holder;
        if(convertView!=null){
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }
        else{
            v = inflater.inflate(id,null);
            final int width= ((GridView)parent).getColumnWidth();
            v.setLayoutParams(new AbsListView.LayoutParams(width, width));
            holder = new ViewHolder();
            holder.thumbnail = (ThumbnailImageView) v.findViewById(R.id.thumbnail);
            v.setTag(holder);
        }

        holder.thumbnail.setImageBitmap(null);

        Bundle b = new Bundle();
        b.putLong(ThumbnailImageView.EXTRA_ID,group.get(position));
        manager.initLoader(ID_IMAGES + position, b, holder.thumbnail);

        return v;
    }
}
