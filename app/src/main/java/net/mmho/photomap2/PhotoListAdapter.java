package net.mmho.photomap2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class PhotoListAdapter extends ArrayAdapter<PhotoGroup> {

    private final int resource;
    private final LayoutInflater inflater;

    public PhotoListAdapter(Context context, int resource, ArrayList<PhotoGroup> objects) {
        super(context, resource, objects);
        this.resource = resource;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            ((PhotoCardLayout) v).setData(g);
        }
        return v;
    }
}
