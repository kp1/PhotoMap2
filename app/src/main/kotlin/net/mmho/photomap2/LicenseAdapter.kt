package net.mmho.photomap2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

internal class LicenseAdapter(context: Context, private val resource: Int, objects: Array<String>)
        : ArrayAdapter<String>(context, resource, objects) {
    private val inflater: LayoutInflater
        = getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View = convertView ?: inflater.inflate(resource, null)
        (v as LicenseLayout).setData(getItem(position))
        return v
    }
}
