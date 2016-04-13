package net.mmho.photomap2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.util.List;

public class SearchResultDialogFragment extends DialogFragment{
    static SearchResultDialogFragment newInstance(String location, List<Address> addresses){
        SearchResultDialogFragment fragment = new SearchResultDialogFragment();


        Bundle bundle = new Bundle();
        bundle.putString("title",location);
        bundle.putParcelableArray("address", addresses.toArray(new Address[addresses.size()]));
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");
        final Address list[] = (Address[]) getArguments().getParcelableArray("address");
        AddressListAdapter adapter = new AddressListAdapter(getActivity(),android.R.layout.simple_list_item_2,list);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.search_title, title));
        builder.setAdapter(adapter, (dialog, which) -> {
            Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
            if(f instanceof PhotoMapFragment){
                final CameraUpdate update = CameraUpdateFactory.newLatLngZoom(AddressUtil.INSTANCE.addressToLatLng(list[which]),
                    PhotoMapFragment.Companion.getDEFAULT_ZOOM());
                ((PhotoMapFragment)f).getMapAsync(map->map.moveCamera(update));
            }
        });
        return builder.create();

    }
}
