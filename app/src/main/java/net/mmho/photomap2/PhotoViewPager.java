package net.mmho.photomap2;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class PhotoViewPager extends ViewPager{

    private GestureDetectorCompat detector;

    public PhotoViewPager(Context context) {
        this(context, null);
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        detector = new GestureDetectorCompat(context,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                performClick();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }

}
