package net.mmho.photomap2;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;


public class PhotoMapActivity extends Activity {

	final static String TAG="MapActivity";

	private GoogleMap mMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_photo_map);

        CustomMapFragment f = (CustomMapFragment)getFragmentManager().findFragmentById(R.id.map);
        mMap = f.getMap();
        if(savedInstanceState==null) loadPreference();

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
