package net.mmho.photomap2

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.Locale

internal class AddressListAdapter(context: Context, private val resource: Int, addresses: Array<Address>)
    : ArrayAdapter<Address>(context, resource, addresses) {
    private val inflater: LayoutInflater
        = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: View = convertView ?: inflater.inflate(resource, null)
        val text1:TextView = v.findViewById(android.R.id.text1)
        val text2:TextView = v.findViewById(android.R.id.text2)
        getItem(position)?.let {
            text1.text = it.getDescription()
            @SuppressLint("SetTextI18n")
            text2.text = "%6.4f,%6.4f".format(Locale.ENGLISH, it.latitude, it.longitude)
        }
        return v
    }


}
