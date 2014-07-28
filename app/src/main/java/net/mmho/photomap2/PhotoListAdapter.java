package net.mmho.photomap2;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private static final String TAG = "PhotoListAdapter";
    private int resource;
    private LayoutInflater inflater;
    private LoaderManager manager;
    private int loader_id;

    public PhotoListAdapter(Context context, int resource, List<PhotoGroup> objects,LoaderManager m,int loader_id_base) {
        super(context, resource, objects);
        this.resource = resource;
        manager = m;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        loader_id = loader_id_base;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(BuildConfig.DEBUG) Log.d(TAG,"getView:"+position);

        View v;
        int id;
        if(convertView!=null){
            v = convertView;
            id = (Integer)v.getTag();
        }
        else {
            v = inflater.inflate(resource,null);
            id = loader_id++;
            v.setTag(id);
        }
        PhotoGroup g = getItem(position);
        ((PhotoCardLayout)v).setData(g, id, manager);

        return v;
    }
}
