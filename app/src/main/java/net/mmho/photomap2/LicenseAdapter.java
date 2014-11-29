package net.mmho.photomap2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class LicenseAdapter extends ArrayAdapter<String> {

    private int resource;
    private LayoutInflater inflater;

    public LicenseAdapter(Context context, int resource,String[] objects) {
        super(context, resource,objects);
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v==null) v = inflater.inflate(resource, null);
        ((LicenseLayout)v).setData(getItem(position));
        return v;
    }
}
