package net.mmho.photomap2;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

public class SearchFragment extends ListFragment{
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Address address = (Address) getListAdapter().getItem(position);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("location", addressToPosition(address));
        intent.putExtras(bundle);
        getActivity().setResult(FragmentActivity.RESULT_OK, intent);
        getActivity().finish();
    }
    // LoaderManager
    private LatLng addressToPosition(Address address){
        return new LatLng(address.getLatitude(),address.getLongitude());
    }

}
