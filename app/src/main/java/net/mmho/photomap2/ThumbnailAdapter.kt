package net.mmho.photomap2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.adapter_thumbnail.view.*

internal class ThumbnailAdapter(c: Context, private val resource: Int, objects: List<HashedPhoto>)
        : ArrayAdapter<HashedPhoto>(c, resource, objects) {

    private val inflater: LayoutInflater

    init {
        inflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View =
            if(position!=0 && convertView!=null) convertView
            else inflater.inflate(resource, null)
        v.thumbnail.startLoading(getItem(position).photoId)
        return v
    }
}
