package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private static final String TAG = "PhotoListAdapter";
    private int id;
    private LayoutInflater inflater;
    private LoaderManager manager;

    public PhotoListAdapter(Context context, int resource, List<PhotoGroup> objects,LoaderManager m) {
        super(context, resource, objects);
        id = resource;
        manager = m;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder{
        TextView title;
        TextView description;
        ImageView thumbnail;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(BuildConfig.DEBUG) Log.d(TAG,"getView:"+position);

        View v;
        ViewHolder holder;
        if(convertView!=null){
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }
        else {
            v = inflater.inflate(id,null);
            holder = new ViewHolder();
            holder.title = (TextView) v.findViewById(R.id.title);
            holder.description = (TextView) v.findViewById(R.id.description);
            holder.thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            v.setTag(holder);
        }
        PhotoGroup g = getItem(position);
        holder.description.setText(g.size()+":"+g.toString());
        holder.thumbnail.setImageBitmap(null);

        ((PhotoCardLayout)v).startLoading(g,position,manager);

        return v;
    }
}
