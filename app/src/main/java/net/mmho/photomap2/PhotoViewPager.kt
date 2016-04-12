package net.mmho.photomap2

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent

class PhotoViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
        : ViewPager(context, attrs) {

    private val detector: GestureDetectorCompat

    init {
        detector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                performClick()
                return true
            }
        })
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        detector.onTouchEvent(ev)
        return super.onTouchEvent(ev)
    }

}
