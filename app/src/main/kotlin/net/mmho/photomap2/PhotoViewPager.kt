package net.mmho.photomap2

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class PhotoViewPager @JvmOverloads constructor(context: Context,attrs: AttributeSet? = null)
    : ViewPager(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}