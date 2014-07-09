package net.mmho.photomap2;


import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


public class PhotoMapActivity extends FragmentActivity {

	final static String TAG="MapActivity";
	final static int SEARCH_PHOTO_DELAY = 250;
    final static int PARTITION_RATIO = 5;

    private static Context context;

	private SupportMapFragment mapFragment;
	private GoogleMap mMap;
	private PhotoCursor photoCursor;
    private Grouping group;

	final private Handler mHandler = new Handler();

	private Runnable delayed = new Runnable() {
		@Override
		public void run() {
            final LatLngBounds b = mMap.getProjection().getVisibleRegion().latLngBounds;
			SearchPhotoQueryTask photoQuery = new SearchPhotoQueryTask(){
				protected void onPostExecute(PhotoCursor result) {
					photoCursor = result;
					if(BuildConfig.DEBUG) Log.d(TAG,"count:"+photoCursor.getCount());
                    if(photoCursor.getCount()!=0){
                        group = new Grouping(photoCursor);
                        group.doGrouping(getPartitionDistance(b));
                        if(BuildConfig.DEBUG) Log.d(TAG,"group:"+group.size());
                    }
					super.onPostExecute(result);
				}
			};
			photoQuery.execute(b);
		}
	};

    private float getPartitionDistance(LatLngBounds b){
        LatLng ne = b.northeast; // north-east
        LatLng sw = b.southwest; // south-west
        float[] d = new float[3];
        Location.distanceBetween(ne.latitude,ne.longitude,sw.latitude,sw.longitude,d);
        return d[0]/PARTITION_RATIO;
    }


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_photo_map);
		mapFragment = (CustomMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
		context = getApplicationContext();
		mMap = mapFragment.getMap();
		
		if(savedInstanceState==null) loadPreference();
		
		mMap.setOnCameraChangeListener(myCameraChangeListener);
		
	}

	public static Context getContext(){
		return context;
	}
	
	OnCameraChangeListener myCameraChangeListener = new OnCameraChangeListener(){
		@Override
		public void onCameraChange(CameraPosition position) {
			if(BuildConfig.DEBUG)Log.d(TAG,position.toString());
			mHandler.removeCallbacks(delayed);
			mHandler.postDelayed(delayed,SEARCH_PHOTO_DELAY);
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
	
}
