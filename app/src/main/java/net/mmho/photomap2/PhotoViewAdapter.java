package net.mmho.photomap2;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class PhotoViewAdapter extends ArrayAdapter<Long>{

    private int resource;
    private LayoutInflater inflater;

    LoaderManager manager;
    int loader_id;

    public PhotoViewAdapter(Context context, int resource, List<Long> objects,LoaderManager manager,int loader_id_base) {
        super(context, resource, objects);
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.manager = manager;
        loader_id = loader_id_base;
    }

    static class ViewHolder{
        PhotoImageView image;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ViewHolder holder;
        int id;
        if(convertView!=null){
            v = convertView;
            holder = (ViewHolder) v.getTag();
            id = (Integer)v.getTag(R.id.photo_view);
        }
        else{
            v = inflater.inflate(resource,null);
            holder = new ViewHolder();
            holder.image = (PhotoImageView) v.findViewById(R.id.photo_view);
            id = loader_id++;
            v.setTag(R.id.photo_view,id);
        }

        holder.image.startLoading(manager,id,getItem(position));

        return v;
    }
}
