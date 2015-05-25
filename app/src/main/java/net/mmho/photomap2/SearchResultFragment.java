package net.mmho.photomap2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.lang.reflect.Field;
import java.util.List;

public class SearchResultFragment extends DialogFragment{
    static SearchResultFragment newInstance(String location, List<Address> addresses){
        SearchResultFragment fragment = new SearchResultFragment();


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
        builder.setAdapter(adapter,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Fragment f = getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
                if(f instanceof PhotoMapFragment){
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(AddressUtil.addressToLatLng(list[which]),
                            PhotoMapFragment.DEFAULT_ZOOM);
                    ((PhotoMapFragment)f).getMap().moveCamera(update);
                }
            }
        });
        return builder.create();

    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            Field mDismissedField = DialogFragment.class.getDeclaredField("mDismissed");
            mDismissedField.setAccessible(true);
            mDismissedField.set(this, true);
            Field mShownByMeField = DialogFragment.class.getDeclaredField("mShownByMe");
            mShownByMeField.setAccessible(true);
            mShownByMeField.set(this, true);
        } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
