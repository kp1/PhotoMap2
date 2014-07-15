package net.mmho.photomap2;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class ThumbnailAdapter extends ArrayAdapter<Long>{

    private static final String TAG = "ThumbnailAdapter";
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
                   MediaStore.Images.Thumbnails.MINI_KIND,null);
        ImageView i = (ImageView)v.findViewById(R.id.thumbnail);

        int width= ((GridView)parent).getColumnWidth();
        v.setLayoutParams(new AbsListView.LayoutParams(width,width));
        i.setImageBitmap(b);

        if(position%2==0) i.setBackgroundColor(Color.LTGRAY);
        return v;
    }
}
