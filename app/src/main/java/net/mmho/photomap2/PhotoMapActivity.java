package net.mmho.photomap2;


import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;

import java.util.List;

public class PhotoMapActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Address>>,
                ProgressChangeListener{

    private static final String TAG_DIALOG = "dialog";
    private final static int ADDRESS_LOADER_ID = 10;
    private final static String SEARCH_QUERY = "query";
    private Dialog dialog = null;
    private ProgressBar progressBar;

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MapSuggestionProvider.AUTHORITY,MapSuggestionProvider.MODE);
            suggestions.saveRecentQuery(searchQuery, null);
            Bundle bundle = new Bundle();
            bundle.putString(SEARCH_QUERY, searchQuery);
            getSupportLoaderManager().restartLoader(ADDRESS_LOADER_ID, bundle, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(result == ConnectionResult.SUCCESS){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
            if(!(fragment instanceof PhotoMapFragment)){
                PhotoMapFragment mapFragment = new PhotoMapFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.map,mapFragment).commit();
            }
        }
        else if(GooglePlayServicesUtil.isUserRecoverableError(result)){
            dialog = GooglePlayServicesUtil.getErrorDialog(result,this,1,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            dialog.show();
        }
        else{
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(dialog!=null && dialog.isShowing()) dialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_photo_map);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if(bar!=null) bar.setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    @Override
    public Loader<List<Address>> onCreateLoader(int i, Bundle bundle) {
        return new GeocodeLoader(this,bundle.getString(SEARCH_QUERY));
    }

    @Override
    public void onLoadFinished(Loader<List<Address>> addressLoader, final List<Address> addresses) {
        final String location =((GeocodeLoader)addressLoader).getLocation();
        if(addresses==null || addresses.size()==0){
            Toast.makeText(getApplicationContext(), getString(R.string.location_not_found,location),
                    Toast.LENGTH_LONG).show();
        }
        else if(addresses.size()==1){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
            CameraUpdate update =
                    CameraUpdateFactory.newLatLngZoom(AddressUtil.addressToLatLng(addresses.get(0)),PhotoMapFragment.DEFAULT_ZOOM);
            if(fragment instanceof PhotoMapFragment) ((PhotoMapFragment)fragment).getMap().moveCamera(update);
        }
        else {
            new Handler(Looper.getMainLooper()).post(new Runnable(){
                @Override
                public void run() {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_DIALOG);
                    if(prev!=null){
                        transaction.remove(prev);
                    }
                    transaction.addToBackStack(null);
                    transaction.commit();

                    SearchResultDialogFragment fragment = SearchResultDialogFragment.newInstance(location, addresses);
                    fragment.show(getSupportFragmentManager(),TAG_DIALOG);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Address>> addressLoader) {

    }

    @Override
    public void showProgress(int progress){
        progressBar.setProgress(progress);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void endProgress(){
        progressBar.setProgress(progressBar.getMax());
        AlphaAnimation fadeout;
        fadeout = new AlphaAnimation(1,0);
        fadeout.setDuration(300);
        fadeout.setFillAfter(true);
        fadeout.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(View.GONE);
                progressBar.setAnimation(null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        progressBar.startAnimation(fadeout);
    }

}
