package net.mmho.photomap2;


import android.app.Dialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PhotoMapActivity extends AppCompatActivity implements ProgressChangeListener{

    private static final String TAG_DIALOG = "dialog";
    private Dialog dialog = null;
    private ProgressBar progressBar;
    public static final int PERMISSIONS_REQUEST = 1;

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Observable.create((Subscriber<? super List<Address>> subscriber) -> {
                List<Address> data = null;
                try {
                    data = new Geocoder(getApplicationContext())
                        .getFromLocationName(searchQuery, 5);
                } catch (IOException e) {
                    subscriber.onError(e);
                }
                if (data != null) subscriber.onNext(data);
                subscriber.onCompleted();
            })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list == null || list.size() == 0) {
                        Toast.makeText(getApplicationContext(), getString(R.string.location_not_found, searchQuery),
                            Toast.LENGTH_LONG).show();
                        return;
                    }

                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        MapSuggestionProvider.AUTHORITY, MapSuggestionProvider.MODE);
                    suggestions.saveRecentQuery(searchQuery, null);

                    if (list.size() == 1) {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
                        CameraUpdate update =
                            CameraUpdateFactory.newLatLngZoom(AddressUtil.addressToLatLng(list.get(0)), PhotoMapFragment.DEFAULT_ZOOM);
                        if (fragment instanceof PhotoMapFragment)
                            ((PhotoMapFragment) fragment).getMap().moveCamera(update);
                    } else {
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        Fragment prev = getSupportFragmentManager().findFragmentByTag(TAG_DIALOG);
                        if (prev != null) transaction.remove(prev);
                        transaction.addToBackStack(null);
                        transaction.commit();

                        SearchResultDialogFragment fragment =
                            SearchResultDialogFragment.newInstance(searchQuery, list);
                        fragment.show(getSupportFragmentManager(), TAG_DIALOG);
                    }
                });
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
            dialog = GooglePlayServicesUtil.getErrorDialog(result,this,1, dialog -> finish());
            dialog.show();
        }
        else{
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.map);
        switch (requestCode){
        case PERMISSIONS_REQUEST:
            boolean granted = grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(fragment!=null && fragment instanceof PhotoMapFragment){
                ((PhotoMapFragment)fragment).grantedPermission(granted);
            }
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
        if(savedInstanceState==null){
            supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        }
        setContentView(R.layout.activity_photo_map);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if(bar!=null) bar.setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progress);
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
