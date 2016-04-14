package net.mmho.photomap2

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.layout_photo_card.view.*


class PhotoCardLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
        : RelativeLayout(context, attrs, defStyle) {

    fun setData(g: PhotoGroup) {
        count.text = "%2d".format(g.size)
        thumbnail.startLoading(g[0].photo_id)
    }

}
