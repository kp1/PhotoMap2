package net.mmho.photomap2

import android.R.layout.simple_list_item_2
import android.app.AlertDialog
import android.app.Dialog
import android.location.Address
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory

class SearchResultDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString("title")
        @Suppress("UNCHECKED_CAST")
        val list = arguments?.getParcelableArray("address") as Array<Address>
        val adapter = AddressListAdapter(requireActivity(),simple_list_item_2, list)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(net.mmho.photomap2.R.string.search_title, title))
        builder.setAdapter(adapter) { _, which ->
            val f = requireActivity().supportFragmentManager.findFragmentById(net.mmho.photomap2.R.id.map)
            if (f is PhotoMapFragment) {
                val update = CameraUpdateFactory.newLatLngZoom(list[which].toLatLng(),
                    PhotoMapFragment.DEFAULT_ZOOM)
                f.getMapAsync { map -> map.moveCamera(update) }
            }
        }
        return builder.create()

    }

    companion object {
        internal fun newInstance(location: String, addresses: List<Address>): SearchResultDialogFragment {
            val fragment = SearchResultDialogFragment()


            val bundle = Bundle()
            bundle.putString("title", location)
            bundle.putParcelableArray("address", addresses.toTypedArray())
            fragment.arguments = bundle
            return fragment
        }
    }
}
