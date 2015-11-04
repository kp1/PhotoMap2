package net.mmho.photomap2;

import android.content.Context;
import android.util.AttributeSet;

public class ThumbnailImageView extends LoadableImageView{

    public ThumbnailImageView(Context context) {
        this(context,null);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        super.thumbnail = true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,widthMeasureSpec);
    }

}
