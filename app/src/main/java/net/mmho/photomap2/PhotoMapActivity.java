package net.mmho.photomap2;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;


public class PhotoMapActivity extends Activity {
    final public static String EXTRA_GROUP="group";

	final private static String TAG="MapActivity";

	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_photo_map);

        PhotoMapFragment f = (PhotoMapFragment)getFragmentManager().findFragmentById(R.id.map);
        mMap = f.getMap();
        Bundle bundle = getIntent().getExtras();
        PhotoGroup group = bundle.getParcelable(EXTRA_GROUP);
        if(group!=null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(expandLatLngBounds(group.getArea(),20),0));
        }
        else if(savedInstanceState==null) loadPreference();

	}


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

    private LatLngBounds expandLatLngBounds(LatLngBounds bounds,double percentile){
        double lat_distance = (bounds.southwest.latitude - bounds.northeast.latitude)*(percentile/2);
        double lng_distance = (bounds.southwest.longitude - bounds.northeast.longitude)*(percentile/2);
        LatLng northeast = new LatLng(bounds.northeast.latitude-lat_distance,bounds.northeast.longitude-lng_distance);
        LatLng southwest = new LatLng(bounds.southwest.latitude+lat_distance,bounds.southwest.longitude+lng_distance);
        return new LatLngBounds(northeast,southwest);
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
