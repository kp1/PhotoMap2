package net.mmho.photomap2;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoMapActivity extends Activity {
    final public static String EXTRA_GROUP="group";
    final private static float DEFAULT_ZOOM = 14;

	private static final String TAG="MapActivity";
    private static final String TAG_MAP = "map";

    private GoogleMap mMap = null;
    private Dialog dialog = null;
    boolean savedInstance = false;


    private final Handler cancelHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            finish();
        }
    };

    private final DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            cancelHandler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onResume()");
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(result == ConnectionResult.SUCCESS){

            setContentView(R.layout.activity_photo_map);
            PhotoMapFragment fragment = (PhotoMapFragment) getFragmentManager().findFragmentById(R.id.map);
            mMap = fragment.getMap();

            if(!savedInstance) {
                final CameraUpdate update = handleIntent(getIntent());

                if (update != null) {
                    //noinspection ConstantConditions
                    fragment.getView().post(new Runnable() {
                        @Override
                        public void run() {
                            mMap.moveCamera(update);
                        }
                    });
                } else loadPreference();
            }
        }
        else if(GooglePlayServicesUtil.isUserRecoverableError(result)){
            dialog = GooglePlayServicesUtil.getErrorDialog(result,this,1,onCancelListener);
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
        savedInstance = savedInstanceState != null;
        Log.d(TAG,"onCreate()");
	}


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private CameraUpdate handleIntent(Intent intent){
        Log.d(TAG,intent.toString());
        if(Intent.ACTION_VIEW.equals(intent.getAction())){
            Uri uri = intent.getData();
            if(uri.getScheme().equals("geo")) {
                String position = uri.toString();
                Pattern pattern = Pattern.compile("(-?\\d+.\\d+),(-?\\d+.\\d+)(\\?z=(\\d+))?");
                Matcher matcher = pattern.matcher(position);
                if(matcher.find() && matcher.groupCount()>=2) {
                    double latitude = Double.parseDouble(matcher.group(1));
                    double longitude = Double.parseDouble(matcher.group(2));
                    float zoom = DEFAULT_ZOOM;

                    if (matcher.groupCount() >= 4) zoom = Integer.parseInt(matcher.group(4));
                    return CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),zoom);
                }
                else{
                    Toast.makeText(this, getString(R.string.no_search_query), Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
        else if(Intent.ACTION_SEND.equals(intent.getAction())){
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            final String[] projection = new String[]{
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE,
            };
            PhotoCursor c = new PhotoCursor(MediaStore.Images.Media.query(getContentResolver(),uri,projection,QueryBuilder.createQuery(),null,null));
            if(c.getCount()==0){
                Toast.makeText(this,getString(R.string.no_position_data),Toast.LENGTH_LONG).show();
                finish();
            }
            c.moveToFirst();
            LatLng position = c.getLocation();
            c.close();
            return CameraUpdateFactory.newLatLngZoom(position,DEFAULT_ZOOM);
        }
        else{
            Bundle bundle = intent.getExtras();
            PhotoGroup group = bundle.getParcelable(EXTRA_GROUP);
            if(group!=null) return CameraUpdateFactory.newLatLngBounds(expandLatLngBounds(group.getArea(), 1.2), 0);
        }
        return null;
    }


	protected void onStop(){
		super.onStop();
		savePreference();
	}

    private LatLngBounds expandLatLngBounds(LatLngBounds bounds,double percentile){
        double lat_distance = (bounds.northeast.latitude - bounds.southwest.latitude)*((percentile-1.0)/2);
        double lng_distance = (bounds.northeast.longitude - bounds.southwest.longitude)*((percentile-1.0)/2);
        LatLng northeast = new LatLng(bounds.northeast.latitude+lat_distance,bounds.northeast.longitude+lng_distance);
        LatLng southwest = new LatLng(bounds.southwest.latitude-lat_distance,bounds.southwest.longitude-lng_distance);
        return new LatLngBounds(southwest,northeast);
    }

    private void loadPreference(){
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        CameraPosition pos = new CameraPosition.Builder().zoom(pref.getFloat("ZOOM",DEFAULT_ZOOM))
                .target(new LatLng((double)pref.getFloat("LATITUDE", 0),(double)pref.getFloat("LONGITUDE",0))).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
    }

    private void savePreference(){
        if(mMap==null) return;
        CameraPosition pos = mMap.getCameraPosition();
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = pref.edit();
        e.putFloat("ZOOM", pos.zoom);
        e.putFloat("LATITUDE",(float)pos.target.latitude);
        e.putFloat("LONGITUDE",(float)pos.target.longitude);
        e.apply();
    }

}
