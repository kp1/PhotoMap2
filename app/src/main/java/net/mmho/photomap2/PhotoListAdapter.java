package net.mmho.photomap2;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Locale;

public class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private int resource;
    private LayoutInflater inflater;

    private AddressFilter filter;
    private ArrayList<PhotoGroup> mOriginalValues;
    private ArrayList<PhotoGroup> mObjects;

    private LruCache<Long,Bitmap> mBitmapCache;

    public PhotoListAdapter(Context context, int resource, ArrayList<PhotoGroup> objects,LruCache<Long,Bitmap> cache) {
        super(context, resource, objects);
        this.resource = resource;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = objects;
        mBitmapCache = cache;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if(convertView!=null){
            v = convertView;
        }
        else {
            v = inflater.inflate(resource,null);
        }
        if(position < getCount()) {
            PhotoGroup g = getItem(position);
            ((PhotoCardLayout) v).setData(g,mBitmapCache);
        }
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
                mOriginalValues = new ArrayList<>(mObjects);
            }
            if(constraint==null || constraint.length()==0){
                result.count = mOriginalValues.size();
                result.values = mOriginalValues;
            }
            else{
                ArrayList<PhotoGroup> filtered = new ArrayList<>();
                for(PhotoGroup group:mOriginalValues){
                    if(group.getDescription().toLowerCase(Locale.getDefault()).contains(String.format("%s", constraint.toString().toLowerCase(Locale.getDefault())))){
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
            if(mOriginalValues==null) return;
            notifyDataSetInvalidated();
            clearData();
            ArrayList<PhotoGroup> list = (ArrayList<PhotoGroup>)results.values;
            for(PhotoGroup g:list) add(g);
            notifyDataSetChanged();
        }
    }

}
