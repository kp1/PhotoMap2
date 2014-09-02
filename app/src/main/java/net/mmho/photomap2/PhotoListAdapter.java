package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private static final String TAG = "PhotoListAdapter";
    private int resource;
    private LayoutInflater inflater;
    private LoaderManager manager;
    private int loader_id;

    private AddressFilter filter;
    private ArrayList<PhotoGroup> mOriginalValues;
    private ArrayList<PhotoGroup> mObjects;

    public PhotoListAdapter(Context context, int resource, ArrayList<PhotoGroup> objects,LoaderManager m,int loader_id_base) {
        super(context, resource, objects);
        this.resource = resource;
        manager = m;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        loader_id = loader_id_base;
        mObjects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

    @Override
    public AddressFilter getFilter() {
        if(filter==null) filter = new AddressFilter();
        return filter;
    }

    public void clear(){
        super.clear();
        mOriginalValues = null;
    }

    private void clearData(){
        super.clear();
    }

    private class AddressFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            if(mOriginalValues==null){
                mOriginalValues = new ArrayList<PhotoGroup>(mObjects);
            }
            if(constraint==null || constraint.length()==0){
                result.count = mOriginalValues.size();
                result.values = mOriginalValues;
            }
            else{
                ArrayList<PhotoGroup> filtered = new ArrayList<PhotoGroup>();
                for(PhotoGroup group:mOriginalValues){
                    if(group.toString().toLowerCase().contains(String.format("%s", constraint.toString().toLowerCase()))){
                        filtered.add(group);
                    }
                }
                result.count = filtered.size();
                result.values = filtered;
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notifyDataSetInvalidated();
            clearData();
            ArrayList<PhotoGroup> list = (ArrayList<PhotoGroup>)results.values;
            addAll(list);
            notifyDataSetChanged();
        }
    }

}
