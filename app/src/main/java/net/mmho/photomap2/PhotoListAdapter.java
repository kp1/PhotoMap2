package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
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

    static class ViewHolder{
        TextView count;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(BuildConfig.DEBUG) Log.d(TAG,"getView:"+position);

        View v;
        ViewHolder holder;
        int id;
        if(convertView!=null){
            v = convertView;
            holder = (ViewHolder)v.getTag();
            id = (Integer)v.getTag(R.id.card);
        }
        else {
            v = inflater.inflate(resource,null);
            holder = new ViewHolder();
            holder.count = (TextView)v.findViewById(R.id.count);
            v.setTag(holder);
            id = loader_id++;
            v.setTag(R.id.card,id);
        }
        PhotoGroup g = getItem(position);
        String s = Integer.toString(g.size());
        holder.count.setText(s);

        ((PhotoCardLayout)v).startLoading(g,id,manager);

        return v;
    }
}
