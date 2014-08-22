package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


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
            title.setText(AddressUtil.getTitle(address, getContext()));
            description.setText(AddressUtil.getDescription(address));
        }

    }

}
