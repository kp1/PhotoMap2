package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private static final int ID_IMAGES = 100;
    private static final int ID_TEXT = 500;
    private int id;
    private List<PhotoGroup> group;
    private LayoutInflater inflater;
    private LoaderManager manager;

    public PhotoListAdapter(Context context, int resource, List<PhotoGroup> objects,LoaderManager m) {
        super(context, resource, objects);
        id = resource;
        group = objects;
        manager = m;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder{
        GeocodeTextView title;
        ThumbnailImageView thumbnail;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        ViewHolder holder;
        if(convertView!=null){
            v = convertView;
            holder = (ViewHolder)v.getTag();
        }
        else {
            v = inflater.inflate(id,null);
            holder = new ViewHolder();
            holder.title = (GeocodeTextView) v.findViewById(R.id.title);
            holder.thumbnail = (ThumbnailImageView) v.findViewById(R.id.thumbnail);
            v.setTag(holder);
        }
        PhotoGroup g = group.get(position);
        holder.title.setText(g.size()+":"+g.toString());
        holder.thumbnail.setImageBitmap(null);

        Bundle imageBundle = new Bundle();
        imageBundle.putLong(ThumbnailImageView.EXTRA_ID, g.getID(0));
        manager.initLoader(ID_IMAGES + position, imageBundle, holder.thumbnail);

        Bundle textBundle = new Bundle();
        textBundle.putParcelable(GeocodeTextView.EXTRA_LOCATION,g.getCenter());
        manager.initLoader(ID_TEXT+position,textBundle,holder.title);


        return v;
    }
}
