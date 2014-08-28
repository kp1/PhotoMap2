package net.mmho.photomap2;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.Loader;
import android.location.Address;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class SearchActivity extends ListActivity implements LoaderManager.LoaderCallbacks<List<Address>>{
    private ArrayAdapter<Address> adapter;
    private String location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            location = intent.getStringExtra(SearchManager.QUERY);
            setTitle(getString(R.string.search_title,location));
            Bundle bundle = new Bundle();
            bundle.putString("location",location);
            getLoaderManager().initLoader(0,bundle,this);
        }
        else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Address address = adapter.getItem(position);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("location",addressToPosition(address));
        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);
        finish();

    }

    // LoaderManager
    private LatLng addressToPosition(Address address){
        return new LatLng(address.getLatitude(),address.getLongitude());
    }

    @Override
    public Loader<List<Address>> onCreateLoader(int id, Bundle args) {
        return new GeocodeLoader(this,args.getString("location"));
    }

    @Override
    public void onLoadFinished(Loader<List<Address>> loader, List<Address> data) {
        if(data==null || data.size()==0){
            Toast.makeText(getApplicationContext(),getString(R.string.location_not_found,location),
                    Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        else if(data.size()==1){
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelable("location",addressToPosition(data.get(0)));
            intent.putExtras(bundle);
            setResult(RESULT_OK,intent);
            finish();
        }
        else {
            adapter = new AddressListAdapter(this, android.R.layout.simple_list_item_2, data);
            setListAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Address>> loader) {

    }
}
