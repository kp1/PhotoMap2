package net.mmho.photomap2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import java.util.ArrayList

internal class PhotoListAdapter(context: Context, private val resource: Int, objects: ArrayList<PhotoGroup>)
        : ArrayAdapter<PhotoGroup>(context, resource, objects) {
    private val inflater: LayoutInflater

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View =
            if(position!=0 && convertView!=null)convertView else inflater.inflate(resource, null)
        if (position < count) (v as PhotoCardLayout).setData(getItem(position))
        return v
    }
}
