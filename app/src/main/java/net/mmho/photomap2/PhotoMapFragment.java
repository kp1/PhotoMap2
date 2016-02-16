package net.mmho.photomap2;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


public class PhotoMapFragment extends SupportMapFragment {
    private final static int PHOTO_CURSOR_LOADER = 0;

    final public static String EXTRA_GROUP="group";
    final public static float DEFAULT_ZOOM = 15;

    private GoogleMap mMap;
    private MenuItem searchMenuItem;
    private ActionBar mActionBar;
    private ArrayList<HashedPhoto> photoList;
    private ArrayList<PhotoGroup> groupList;

    private ProgressChangeListener listener;


    private PublishSubject<Integer> subject;
    private Subscription subscription;

    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
        setHasOptionsMenu(true);
        photoList = new ArrayList<>();
        groupList = new ArrayList<>();
        subject = PublishSubject.create();
	}

    @Override
    public void onStart() {
        super.onStart();
        if(subscription==null){
            subscription =
                subject
                    .onBackpressureLatest()
                    .switchMap(this::groupObservable)
                    .subscribe();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(subscription!=null){
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(!(activity instanceof ProgressChangeListener)){
            throw new RuntimeException(activity.getLocalClassName()+" must implement ProgressChangeListener");
        }
        listener = (ProgressChangeListener)activity;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode){
        case AppCompatActivity.RESULT_OK:
            LatLng position = data.getExtras().getParcelable("location");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,DEFAULT_ZOOM));
            break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_map_menu, menu);

        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchMenuItem = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, actionExpandListener);
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if(!hasFocus) MenuItemCompat.collapseActionView(searchMenuItem);
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryRefinementEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_history:
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                        MapSuggestionProvider.AUTHORITY,MapSuggestionProvider.MODE);
                suggestions.clearHistory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final private MenuItemCompat.OnActionExpandListener actionExpandListener =
            new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    showActionBar(false);
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    hideActionBarDelayed();
                    return true;
                }
            };


    final private SearchView.OnQueryTextListener onQueryTextListener =
            new SearchView.OnQueryTextListener(){

                @Override
                public boolean onQueryTextSubmit(String query) {
                    MenuItemCompat.collapseActionView(searchMenuItem);
                    requestQuery(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            };


    private void requestQuery(String query){
        Intent intent = new Intent(getActivity(),PhotoMapActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY,query);
        startActivity(intent);
    }

    private LatLngBounds expandLatLngBounds(LatLngBounds bounds, double percentile){
        double lat_distance = (bounds.northeast.latitude - bounds.southwest.latitude)*((percentile-1.0)/2);
        double lng_distance = (bounds.northeast.longitude - bounds.southwest.longitude)*((percentile-1.0)/2);
        LatLng northeast = new LatLng(bounds.northeast.latitude+lat_distance,bounds.northeast.longitude+lng_distance);
        LatLng southwest = new LatLng(bounds.southwest.latitude-lat_distance,bounds.southwest.longitude-lng_distance);
        return new LatLngBounds(southwest,northeast);
    }

    private CameraUpdate handleIntent(Intent intent){
        if(Intent.ACTION_VIEW.equals(intent.getAction())){
            Uri uri = intent.getData();
            if(uri.getScheme().equals("geo")) {
                String position = uri.toString();
                Pattern pattern = Pattern.compile("(-?\\d+.\\d+),(-?\\d+.\\d+)(\\?([zq])=(.*))?");
                Matcher matcher = pattern.matcher(position);
                if(matcher.find() && matcher.groupCount()>=2) {
                    double latitude = Double.parseDouble(matcher.group(1));
                    double longitude = Double.parseDouble(matcher.group(2));
                    float zoom = DEFAULT_ZOOM;

                    if (matcher.groupCount() == 5 && matcher.group(4)!=null){
                        if (matcher.group(4).equals("z")) {
                            zoom = Integer.parseInt(matcher.group(5));
                        } else if (matcher.group(4).equals("q")) {
                            requestQuery(matcher.group(5));
                            return null;
                        }
                    }
                    return CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),zoom);
                }
            }
        }
        else if(Intent.ACTION_SEND.equals(intent.getAction())){
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            final String[] projection = new String[]{
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE,
            };
            PhotoCursor c = new PhotoCursor(MediaStore.Images.Media.query(getActivity().getContentResolver(),uri,projection,QueryBuilder.createQuery(),null,null));
            if(c.getCount()==0){
                Toast.makeText(getActivity(),getString(R.string.no_position_data),Toast.LENGTH_LONG).show();
                getActivity().finish();
                return null;
            }
            c.moveToFirst();
            LatLng position = c.getLocation();
            c.close();
            return CameraUpdateFactory.newLatLngZoom(position,DEFAULT_ZOOM);
        }
        else{
            Bundle bundle = intent.getExtras();
            PhotoGroup group = bundle.getParcelable(EXTRA_GROUP);
            if(group!=null) {
                return CameraUpdateFactory.newLatLngBounds(expandLatLngBounds(group.getHash().getBounds(),1.2),0);
            }
        }
        return null;
    }

    private void initMap(){

        if(mMap!=null){
            getLoaderManager().initLoader(PHOTO_CURSOR_LOADER, null, photoListLoaderCallback);
            return;
        }

        getMapAsync(map -> {
            mMap = map;
            Intent intent = getActivity().getIntent();
            final CameraUpdate update = handleIntent(intent);
            if (update != null && getView() != null) {
                getView().post(() -> mMap.moveCamera(update));
            }
            mMap.setOnCameraChangeListener(photoMapCameraChangeListener);
            mMap.setOnMarkerClickListener(marker -> {
                Observable.from(groupList)
                    .filter(g -> g.marker.equals(marker))
                    .first()
                    .subscribe(g -> {
                        Intent i;
                        if (g.size() == 1) {
                            i = new Intent(getActivity(), PhotoViewActivity.class);
                            i.putExtra(PhotoViewActivity.EXTRA_GROUP, (Parcelable) g);
                        } else {
                            i = new Intent(getActivity(), ThumbnailActivity.class);
                            i.putExtra(ThumbnailActivity.EXTRA_GROUP, (Parcelable) g);
                        }
                        startActivity(i);
                    });
                    return true;
            });
            mMap.setOnMapClickListener(latLng -> {
                if (mActionBar.isShowing()) hideActionBar();
                else showActionBar(true);
            });
            mMap.getUiSettings().setZoomControlsEnabled(false);
            getLoaderManager().initLoader(PHOTO_CURSOR_LOADER, null, photoListLoaderCallback);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= 23
            && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PhotoMapActivity.PERMISSIONS_REQUEST);
            }
            else{
                View v = getView();
                if(v!=null) PermissionUtils.requestPermission(v,getContext());
            }
        }
        else {
            initMap();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(mActionBar!=null) mActionBar.addOnMenuVisibilityListener(visible -> {
            if(visible) showActionBar(false);
            else hideActionBarDelayed();
        });

    }

    private final Handler handler = new Handler();
    private final Runnable runnable= this::hideActionBar;

    private void showActionBar(boolean hide){
        mActionBar.show();
        if(!hide) handler.removeCallbacks(runnable);
        else hideActionBarDelayed();
    }

    private void hideActionBar(){
        mActionBar.hide();
    }

    private void hideActionBarDelayed(){
        final long DELAY = 3*1000;  // 3sec
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, DELAY);
    }

    private final static int MAXIMUM_ZOOM = 17;
    private final static int MINIMUM_ZOOM = 4;

    private final GoogleMap.OnCameraChangeListener photoMapCameraChangeListener=
        new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if(position.zoom> MAXIMUM_ZOOM || position.zoom<MINIMUM_ZOOM){
                    float zoom = position.zoom> MAXIMUM_ZOOM ? MAXIMUM_ZOOM :MINIMUM_ZOOM;
                    mMap.setOnCameraChangeListener(null);
                    CameraUpdate cameraUpdate =
                            CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(position.target,zoom));
                    mMap.animateCamera(cameraUpdate, cancelableCallback);
                    return;
                }
                showActionBar(false);
                getLoaderManager().restartLoader(PHOTO_CURSOR_LOADER, null, photoListLoaderCallback);
            }
        };

    private final GoogleMap.CancelableCallback cancelableCallback =
        new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mMap.setOnCameraChangeListener(photoMapCameraChangeListener);
            }

            @Override
            public void onCancel() {
                mMap.setOnCameraChangeListener(photoMapCameraChangeListener);
            }
        };

    private final LoaderManager.LoaderCallbacks<Cursor> photoListLoaderCallback =
        new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                LatLngBounds mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                String q = QueryBuilder.createQuery(mapBounds);
                String o = QueryBuilder.sortDateNewest();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                return new CursorLoader(getActivity(),uri, PhotoCursor.projection, q, null, o);

            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                photoList = new PhotoCursor(cursor).getHashedPhotoList();
                int distance = (int)(mMap.getCameraPosition().zoom*2+4);
                if(distance>45) distance = 45;
                subject.onNext(distance);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> objectLoader) {
            }
        };

    private int progress;
    private int group_count;
    private Observable<PhotoGroup> groupObservable(int distance){
        return Observable.from(photoList)
            .subscribeOn(Schedulers.newThread())
            .groupBy(hash -> hash.getHash().getBinaryString().substring(0, distance))
            .doOnNext(g -> group_count++)
            .concatMap(group -> group.map(PhotoGroup::new)
                .reduce(PhotoGroup::append))
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe(() -> {
                mMap.clear();
                progress = group_count = 0;
                listener.showProgress(0);
                groupList.clear();
            })
            .doOnNext(g -> {
                listener.showProgress(++progress * 10000 / group_count);
                groupList.add(g);
                MarkerOptions ops = new MarkerOptions().position(g.getCenter());
                ops.icon(BitmapDescriptorFactory.defaultMarker(PhotoGroup.getMarkerColor(g.size())));
                g.marker = mMap.addMarker(ops);
            })
            .doOnCompleted(() -> {
                hideActionBarDelayed();
                listener.endProgress();
            });
    }

    public void grantedPermission(boolean granted) {
        if(granted){
            initMap();
        }
        else{
            View v = getView();
            if(v!=null)PermissionUtils.requestPermission(v,getContext());
        }
    }
}
