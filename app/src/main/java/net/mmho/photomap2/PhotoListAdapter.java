package net.mmho.photomap2;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kp1 on 2014/07/10.
 */
public class PhotoListAdapter extends ArrayAdapter<PhotoGroup.Group> {

    private Context context;
    private int id;
    private List<PhotoGroup.Group> group;
    private LayoutInflater inflater;

    public PhotoListAdapter(Context c, int resource, List<PhotoGroup.Group> objects) {
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
        PhotoGroup.Group g = group.get(position);
        ((TextView) v.findViewById(R.id.title)).setText(g.size()+":"+g.getCenter().toString());
        return v;
    }
}
