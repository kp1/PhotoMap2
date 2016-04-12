package net.mmho.photomap2

import android.content.Context
import android.util.AttributeSet

class ThumbnailImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
        : LoadableImageView(context, attrs, defStyle) {

    init {
        super.thumbnail = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthMeasureSpec, widthMeasureSpec)
    }

}
