package net.mmho.photomap2

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

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val v: View = convertView ?: inflater.inflate(resource, null)
        val text1 = v.findViewById(android.R.id.text1) as TextView
        val text2 = v.findViewById(android.R.id.text2) as TextView
        val address = getItem(position)
        text1.text = AddressUtil.getDescription(address)
        text2.text = String.format(Locale.getDefault(), "%6.4f,%6.4f", address.latitude, address.longitude)
        return v
    }


}
