package net.mmho.photomap2

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.layout_photo_card.view.*


class PhotoCardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
        : RelativeLayout(context, attrs, defStyle) {

    fun setData(g: PhotoGroup) {
        count.text = String.format("%2d", g.size)
        thumbnail.startLoading(g[0].photoId)
    }

}
