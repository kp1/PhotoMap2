package net.mmho.photomap2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class PhotoCardLayout extends RelativeLayout {

    private ThumbnailImageView thumbnail;
    private TextView count;

    public PhotoCardLayout(Context context) {
        this(context,null);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoCardLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        thumbnail = (ThumbnailImageView)findViewById(R.id.thumbnail);
        count = (TextView)findViewById(R.id.count);
    }

    public void setData(PhotoGroup g){
        count.setText(String.format("%2d",g.size()));
        thumbnail.startLoading(g.get(0).getPhotoId());
    }

}
