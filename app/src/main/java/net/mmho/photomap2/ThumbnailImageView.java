package net.mmho.photomap2;

import android.content.Context;
import android.util.AttributeSet;

public class ThumbnailImageView extends LoadableImageView{

    public ThumbnailImageView(Context context) {
        super(context);
        super.thumbnail = true;
    }

    public ThumbnailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.thumbnail = true;

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
