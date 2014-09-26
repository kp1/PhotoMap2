package net.mmho.photomap2;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoMapFragment extends SupportMapFragment {
	final static int PARTITION_RATIO = 6;
    final static int PHOTO_CURSOR_LOADER = 0;
    final static int PHOTO_GROUP_LOADER = 1;

    final public static String EXTRA_GROUP="group";
    final private static float DEFAULT_ZOOM = 15;
    private static final String TAG = "PhotoMapFragment";

    private GoogleMap mMap;
    private LatLngBounds mapBounds;
    private PhotoCursor photoCursor;
    private PhotoGroupList mGroup;
    private int progress;
    private MarkerOptions sharedMarker;
    private MenuItem searchMenuItem;
    private ActionBar mActionBar;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
        setHasOptionsMenu(true);
        mGroup = new PhotoGroupList();
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult():"+resultCode);
        switch(resultCode){
        case ActionBarActivity.RESULT_OK:
            LatLng position = data.getExtras().getParcelable("location");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,DEFAULT_ZOOM));
            break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_map_menu,menu);

        SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setOnQueryTextListener(onQueryTextListener);
        searchMenuItem = menu.findItem(R.id.search);
        MenuItemCompat.setOnActionExpandListener(searchMenuItem,actionExpandListener);
        searchView.setOnQueryTextFocusChangeListener(onFocusChangeListener);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
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
                    hideActionBarDelayed(HIDE_DELAY);
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

    final private SearchView.OnFocusChangeListener onFocusChangeListener =
            new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus) MenuItemCompat.collapseActionView(searchMenuItem);
                }
            };

    private void requestQuery(String query){
        Intent intent = new Intent(getActivity(),SearchActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY,query);
        startActivityForResult(intent,0);
    }

    private LatLngBounds expandLatLngBounds(LatLngBounds bounds,double percentile){
        double lat_distance = (bounds.northeast.latitude - bounds.southwest.latitude)*((percentile-1.0)/2);
        double lng_distance = (bounds.northeast.longitude - bounds.southwest.longitude)*((percentile-1.0)/2);
        LatLng northeast = new LatLng(bounds.northeast.latitude+lat_distance,bounds.northeast.longitude+lng_distance);
        LatLng southwest = new LatLng(bounds.southwest.latitude-lat_distance,bounds.southwest.longitude-lng_distance);
        return new LatLngBounds(southwest,northeast);
    }

    private Bitmap createBitMap(int resource){
        int height = getResources().getDimensionPixelSize(R.dimen.marker_height);
        int width = getResources().getDimensionPixelSize(R.dimen.marker_width);
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable shape = getResources().getDrawable(resource);
        shape.setBounds(0,0,bitmap.getWidth(),bitmap.getHeight());
        shape.draw(canvas);
        return bitmap;
    }

    private CameraUpdate handleIntent(Intent intent){
        sharedMarker =null;
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

                    if (matcher.groupCount() == 5){
                        if(matcher.group(4).equals("z")) {
                            zoom = Integer.parseInt(matcher.group(5));
                        }
                        else if(matcher.group(4).equals("q")){
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
            }
            c.moveToFirst();
            LatLng position = c.getLocation();
            c.close();
            sharedMarker = new MarkerOptions();
            sharedMarker.icon(BitmapDescriptorFactory.fromBitmap(createBitMap(R.drawable.dot)))
                    .position(position);
            return CameraUpdateFactory.newLatLngZoom(position,DEFAULT_ZOOM);
        }
        else{
            Bundle bundle = intent.getExtras();
            PhotoGroup group = bundle.getParcelable(EXTRA_GROUP);
            if(group!=null) return CameraUpdateFactory.newLatLngBounds(expandLatLngBounds(group.getArea(), 1.2), 0);
        }
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(mMap==null){
            mMap = getMap();
            Intent intent = getActivity().getIntent();
            final CameraUpdate update = handleIntent(intent);
            if(update!=null && getView()!=null) {
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        mMap.moveCamera(update);
                    }
                });
            }
            mMap.setOnCameraChangeListener(photoMapCameraChangeListener);
            mMap.setOnMarkerClickListener(photoGroupClickListener);
            mMap.setOnMapClickListener(photoMapClickListener);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            getLoaderManager().initLoader(PHOTO_CURSOR_LOADER, null, photoListLoaderCallback);
        }

        mActionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();

    }

    final long HIDE_DELAY = 3*1000;  // 3sec

    final Handler ab_handler = new Handler();
    final Runnable runnable= new Runnable() {
        @Override
        public void run() {
            hideActionBar();
        }
    };

    private void showActionBar(boolean hide){
        mActionBar.show();
        if(!hide)ab_handler.removeCallbacks(runnable);
        else hideActionBarDelayed(HIDE_DELAY);
    }

    private void hideActionBar(){
        mActionBar.hide();
    }

    private void hideActionBarDelayed(long delay){
        ab_handler.removeCallbacks(runnable);
        ab_handler.postDelayed(runnable,delay);
    }

    GoogleMap.OnMapClickListener photoMapClickListener =
        new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(mActionBar.isShowing()) hideActionBar();
                else showActionBar(true);
            }
        };

    GoogleMap.OnCameraChangeListener photoMapCameraChangeListener=
        new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if(position.zoom>16 || position.zoom<3){
                    float zoom = position.zoom>16?16:3;
                    mMap.setOnCameraChangeListener(null);
                    CameraUpdate cameraUpdate =
                            CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(position.target,zoom));
                    mMap.animateCamera(cameraUpdate, cancelableCallback);
                    return;
                }
                showActionBar(false);
                getLoaderManager().destroyLoader(PHOTO_GROUP_LOADER);
                getLoaderManager().restartLoader(PHOTO_CURSOR_LOADER, null,photoListLoaderCallback);
            }
        };

    GoogleMap.CancelableCallback cancelableCallback =
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

    GoogleMap.OnMarkerClickListener photoGroupClickListener =
        new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for(PhotoGroup group:mGroup){
                    if(group.marker.equals(marker)){
                        Intent intent;
                        if(group.size()==1){
                            intent = new Intent(getActivity(),PhotoViewActivity.class);
                            intent.putExtra(PhotoViewActivity.EXTRA_GROUP,group);
                        }
                        else{
                            intent = new Intent(getActivity(),ThumbnailActivity.class);
                            intent.putExtra(ThumbnailActivity.EXTRA_GROUP,group);
                        }
                        startActivity(intent);
                        break;
                    }
                }
                return true;
            }
        };

    private float getPartitionDistance(LatLngBounds b){
        LatLng ne = b.northeast; // north-east
        LatLng sw = b.southwest; // south-west
        float[] d = new float[3];
        Location.distanceBetween(ne.latitude, ne.longitude, sw.latitude, sw.longitude, d);
        return d[0]/PARTITION_RATIO;
    }

    private void setProgress(int progress){
        getActivity().setProgress(progress);
        getActivity().setProgressBarVisibility(true);
    }

    private void endProgress(){
        getActivity().setProgress(Window.PROGRESS_END);
        getActivity().setProgressBarVisibility(false);
    }

    final private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(isRemoving()) return;
            switch(msg.what){
                case PhotoGroupList.MESSAGE_RESTART:
                    progress = 0;
                    break;
                case PhotoGroupList.MESSAGE_APPEND:
                    progress++;
                    setProgress(progress * Window.PROGRESS_END/photoCursor.getCount());
                    break;
            }
        }
    };

    LoaderManager.LoaderCallbacks<Cursor> photoListLoaderCallback =
        new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                String q = QueryBuilder.createQuery(mapBounds);
                String o = QueryBuilder.sortDateNewest();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                return new CursorLoader(getActivity(),uri, PhotoCursor.projection, q, null, o);

            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                photoCursor = new PhotoCursor(cursor);
                if(photoCursor.getCount()!=0) {
                    getLoaderManager().destroyLoader(PHOTO_GROUP_LOADER);
                    getLoaderManager().restartLoader(PHOTO_GROUP_LOADER, null, photoGroupListLoaderCallbacks);
                }
                else{
                    mMap.clear();
                    if(sharedMarker!=null)mMap.addMarker(sharedMarker);
                    hideActionBarDelayed(HIDE_DELAY);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> objectLoader) {
            }
        };

    LoaderManager.LoaderCallbacks<PhotoGroupList> photoGroupListLoaderCallbacks =
        new LoaderManager.LoaderCallbacks<PhotoGroupList>() {
            @Override
            public Loader<PhotoGroupList> onCreateLoader(int id, Bundle args) {
                return new PhotoGroupListLoader(getActivity(),
                        mGroup,photoCursor,getPartitionDistance(mapBounds),true,handler);
            }

            @Override
            public void onLoadFinished(Loader<PhotoGroupList> loader, PhotoGroupList data) {
                endProgress();
                hideActionBarDelayed(HIDE_DELAY);
                mMap.clear();
                for(PhotoGroup group:mGroup){
                    MarkerOptions ops = new MarkerOptions().position(group.getCenter());
                    ops.icon(BitmapDescriptorFactory.defaultMarker(PhotoGroup.getMarkerColor(group.size())));
                    group.marker = mMap.addMarker(ops);
                }
                if(sharedMarker !=null)mMap.addMarker(sharedMarker);
            }

            @Override
            public void onLoaderReset(Loader<PhotoGroupList> loader) {

            }
        };

}
