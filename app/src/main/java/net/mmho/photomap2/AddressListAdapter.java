package net.mmho.photomap2;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AddressListAdapter extends ArrayAdapter<Address>{

    private int resource;
    private LayoutInflater inflater;

    public AddressListAdapter(Context context, int resource,Address[] addresses) {
        super(context,resource,addresses);
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        if(convertView!=null){
            v = convertView;
        }
        else{
            v = inflater.inflate(resource,null);
        }
        TextView text1 = (TextView) v.findViewById(android.R.id.text1);
        TextView text2 = (TextView) v.findViewById(android.R.id.text2);
        Address address = getItem(position);
        text1.setText(AddressUtil.getDescription(address));
        text2.setText(String.format("%6.4f,%6.4f", address.getLatitude(), address.getLongitude()));
        return v;
    }


}
