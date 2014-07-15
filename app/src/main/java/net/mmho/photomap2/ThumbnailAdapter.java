package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.List;

public class ThumbnailAdapter extends ArrayAdapter<Long>{

    private Context context;
    private int id;
    private List<Long> group;
    private LayoutInflater inflater;

    public ThumbnailAdapter(Context c, int resource, List<Long> objects) {
        super(c, resource, objects);
        context = c;
        id = resource;
        group = objects;
        inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if(convertView!=null){
            v = convertView;
        }
        else{
            v = inflater.inflate(id,null);
        }
        Bitmap b = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),group.get(position),
                   MediaStore.Images.Thumbnails.MICRO_KIND,null);
        ((ImageView)v.findViewById(R.id.thumbnail)).setImageBitmap(b);
        return v;
    }
}
