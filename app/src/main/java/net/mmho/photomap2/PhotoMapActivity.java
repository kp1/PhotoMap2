package net.mmho.photomap2;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class PhotoMapActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	final static String TAG="MapActivity";
    final static int PARTITION_RATIO = 10;

    private static Context context;

	private SupportMapFragment mapFragment;
	private GoogleMap mMap;
    private LatLngBounds mapBounds;
	private PhotoCursor photoCursor;
    private Grouping mGroup;


    private float getPartitionDistance(LatLngBounds b){
        LatLng ne = b.northeast; // north-east
        LatLng sw = b.southwest; // south-west
        float[] d = new float[3];
        Location.distanceBetween(ne.latitude,ne.longitude,sw.latitude,sw.longitude,d);
        return d[0]/PARTITION_RATIO;
    }

    private String createQueryOrder(double latitude,double longitude) {
        StringBuilder b = new StringBuilder();
        b.append("((latitude-(").append(latitude).append("))*(latitude-(").append(latitude).append(")))+");
        b.append("((longitude-(").append(longitude).append("))*(longitude-(").append(longitude).append("))) ");
        b.append("asc limit 50");
        return new String(b);
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_photo_map);
		mapFragment = (CustomMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		context = getApplicationContext();
		mMap = mapFragment.getMap();

        getSupportLoaderManager().initLoader(0,null,this);


		if(savedInstanceState==null) loadPreference();
		
		mMap.setOnCameraChangeListener(myCameraChangeListener);
		
	}

	public static Context getContext(){
		return context;
	}
	
	OnCameraChangeListener myCameraChangeListener = new OnCameraChangeListener(){
		@Override
		public void onCameraChange(CameraPosition position) {
            mMap.clear();
			if(BuildConfig.DEBUG)Log.d(TAG,position.toString());
            getSupportLoaderManager().restartLoader(0, null, PhotoMapActivity.this);
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	protected void onStop(){
		super.onStop();
		savePreference();
	}
	
	private void loadPreference(){
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		CameraPosition pos = new CameraPosition.Builder().zoom(pref.getFloat("ZOOM", 4))
		.target(new LatLng((double)pref.getFloat("LATITUDE", 0),(double)pref.getFloat("LONGITUDE",0))).build();
		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
	}
	
	private void savePreference(){
		CameraPosition pos = mMap.getCameraPosition();
		SharedPreferences pref = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor e = pref.edit();
		e.putFloat("ZOOM", pos.zoom);
		e.putFloat("LATITUDE",(float)pos.target.latitude);
		e.putFloat("LONGITUDE",(float)pos.target.longitude);
		e.apply();
	}

    private String createQueryString(LatLngBounds bounds) {
        LatLng start = bounds.southwest;
        LatLng end = bounds.northeast;
        StringBuilder b = new StringBuilder();
        b.append("(latitude between ").append(start.latitude).append(" and ").append(end.latitude).append(")");
        b.append(" and (longitude between ").append(start.longitude).append(" and ").append(end.longitude).append(")");
        return new String(b);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        String q = createQueryString(mapBounds);
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(getContext(),uri, PhotoCursor.projection, q, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(BuildConfig.DEBUG) Log.d(TAG,"count:"+cursor.getCount());
        photoCursor = new PhotoCursor(cursor);
        mGroup = new Grouping(photoCursor);
        mGroup.doGrouping(getPartitionDistance(mapBounds));
        if(BuildConfig.DEBUG) Log.d(TAG,"group:"+mGroup.size());
        for(PhotoGroup p:mGroup){
            mMap.addMarker(new MarkerOptions().position(p.getCenter()).title(String.valueOf(p.size())));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {

    }
}
