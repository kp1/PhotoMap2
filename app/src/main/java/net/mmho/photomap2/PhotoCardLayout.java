package net.mmho.photomap2;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.support.v4.app.LoaderManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoCardLayout extends RelativeLayout{

    private static final String TAG = "PhotoCardLayout";

    private ThumbnailImageView thumbnail;
    private TextView title;
    private TextView description;
    private TextView count;
    private ImageView menu;

    private PhotoGroup group = null;

    public PhotoCardLayout(Context context) {
        super(context);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        thumbnail = (ThumbnailImageView)findViewById(R.id.thumbnail);
        title = (TextView)findViewById(R.id.title);
        description = (TextView)findViewById(R.id.description);
        menu = (ImageView)findViewById(R.id.overflow);
        menu.setOnClickListener(onClickListener);
        count = (TextView)findViewById(R.id.count);
    }

    OnClickListener onClickListener =
        new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),PhotoMapActivity.class);
                i.putExtra(PhotoMapActivity.EXTRA_GROUP,group);
                getContext().startActivity(i);
            }
        };

    public void setData(PhotoGroup g, int loader_id, LoaderManager manager){
        group = g;

        count.setText(String.format("%2d",g.size()));

        thumbnail.startLoading(manager,loader_id,g.getID(0));

        Address address = g.address;
        if(address==null) {
            title.setText(R.string.loading);
            description.setText(g.toString());
        }
        else{
            title.setText(addressToTitle(address));
            description.setText(addressToDescription(address));
        }

    }

    private String removePostalCode(String source){
        return source.replaceFirst("〒[0-9¥-]*","");
    }

    private String addressToDescription(Address address){
        String description;
        if(address.getMaxAddressLineIndex()>0){
            description = address.getAddressLine(1);
        }
        else{
            description = address.getAddressLine(0);
        }
        return removePostalCode(description);
    }

    private String addressToTitle(Address address){
        StringBuilder builder = new StringBuilder();
        final String separator = getContext().getString(R.string.address_separator);
        // TODO: change address order with Language setting.

        if(address.getAdminArea()!=null){
            builder.append(address.getAdminArea());
            builder.append(separator);
        }
        if(address.getSubAdminArea()!=null){
            builder.append(address.getSubAdminArea());
            builder.append(separator);
        }
        if(address.getLocality()!=null){
            builder.append(address.getLocality());
        }
        if(builder.length()==0){
            builder.append((address.getFeatureName()));
        }

        return new String(builder);
    }
}
