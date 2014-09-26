package net.mmho.photomap2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.support.v4.app.LoaderManager;
import android.support.v4.util.LruCache;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PhotoCardLayout extends RelativeLayout{

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

    private void moveMap(){
        Intent i = new Intent(getContext(),PhotoMapActivity.class);
        i.putExtra(PhotoMapFragment.EXTRA_GROUP,group);
        getContext().startActivity(i);
    }
    final ImageView.OnClickListener onClickListener =
        new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(),v);
                popup.setOnMenuItemClickListener(popupClickListener);
                popup.inflate(R.menu.photo_list_popup_menu);
                popup.show();
            }
        };

    final PopupMenu.OnMenuItemClickListener popupClickListener =
        new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                case R.id.map:
                    moveMap();
                    return true;
                default:
                    return false;
                }
            }
        };

    public void setData(PhotoGroup g, int loader_id, LoaderManager manager,LruCache<Long,Bitmap> cache){
        group = g;

        count.setText(String.format("%2d",g.size()));

        thumbnail.startLoading(manager,loader_id,g.getID(0),cache);

        Address address = g.address;
        if(address==null) {
            title.setText(R.string.loading);
            description.setText(g.locationToString());
        }
        else{
            title.setText(AddressUtil.getTitle(address, getContext()));
            description.setText(AddressUtil.getDescription(address));
        }

    }

}
