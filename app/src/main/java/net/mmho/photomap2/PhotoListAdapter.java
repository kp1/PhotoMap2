package net.mmho.photomap2;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private Context context;
    private int id;
    private List<PhotoGroup> group;
    private LayoutInflater inflater;

    public PhotoListAdapter(Context c, int resource, List<PhotoGroup> objects) {
        super(c, resource, objects);
        context = c;
        id = resource;
        group = objects;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        Log.d("adapter","getView:"+position );
        if(convertView!=null){
            v = convertView;
        }
        else {
            v = inflater.inflate(id,null);
        }
        PhotoGroup g = group.get(position);
        ((TextView) v.findViewById(R.id.title)).setText(g.getID(0) + ":" + g.toString());
        Bitmap b = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(),g.getID(0), MediaStore.Images.Thumbnails.MICRO_KIND,null);
        ((ImageView)v.findViewById(R.id.thumbnail)).setImageBitmap(b);
        Configuration c = context.getResources().getConfiguration();
        boolean landscape = c.orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean color;
        if(landscape) color = position % 4 == 1 || position % 4 == 2;
        else color = position%2==1;
        if(color) v.setBackgroundColor(Color.LTGRAY);
        else v.setBackgroundColor(Color.WHITE);

        return v;
    }
}
