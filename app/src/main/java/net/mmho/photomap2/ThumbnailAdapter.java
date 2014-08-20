package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class ThumbnailAdapter extends ArrayAdapter<Long>{

    private static final String TAG = "ThumbnailAdapter";
    private int resource;
    private LayoutInflater inflater;
    private LoaderManager manager;
    private int loader_id;

    public ThumbnailAdapter(Context c, int resource, List<Long> objects,LoaderManager m,int loader_id_base) {
        super(c, resource, objects);
        this.resource = resource;
        manager = m;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        loader_id = loader_id_base;
    }

    private class ViewHolder{
        ThumbnailImageView thumbnail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ViewHolder holder;
        int id;
        if(convertView!=null){
            v = convertView;
            holder = (ViewHolder) v.getTag();
            id = (Integer)v.getTag(R.id.thumbnail);
        }
        else{
            v = inflater.inflate(resource,null);
            holder = new ViewHolder();
            holder.thumbnail = (ThumbnailImageView) v.findViewById(R.id.thumbnail);
            v.setTag(holder);
            id=loader_id++;
            v.setTag(R.id.thumbnail,id);
        }

        holder.thumbnail.startLoading(manager,id,getItem(position));

        return v;
    }
}
