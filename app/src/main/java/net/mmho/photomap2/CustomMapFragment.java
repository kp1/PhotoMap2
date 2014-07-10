package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class CustomMapFragment extends MapFragment implements LoaderManager.LoaderCallbacks<Cursor>{
	final static String TAG="CustomMapFragment";
	final static int PARTITION_RATIO = 10;

    private GoogleMap mMap;
    private LatLngBounds mapBounds;
    private PhotoCursor photoCursor;
    private PhotoGroup mGroup;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		if(savedInstanceState==null){
			setRetainInstance(true);
		}
	}

    @Override
    public void onResume() {
        super.onResume();
        mMap = getMap();
        mMap.setOnCameraChangeListener(myCameraChangeListener);
        getLoaderManager().initLoader(0,null,this);
    }

    GoogleMap.OnCameraChangeListener myCameraChangeListener=
        new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                mMap.clear();
                if(BuildConfig.DEBUG)Log.d(TAG,position.toString());
                getLoaderManager().restartLoader(0, null, CustomMapFragment.this);
            }
        };

    private float getPartitionDistance(LatLngBounds b){
        LatLng ne = b.northeast; // north-east
        LatLng sw = b.southwest; // south-west
        float[] d = new float[3];
        Location.distanceBetween(ne.latitude, ne.longitude, sw.latitude, sw.longitude, d);
        return d[0]/PARTITION_RATIO;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        String q = QueryBuilder.createQuery(mapBounds);
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        return new CursorLoader(getActivity().getApplicationContext(),uri, PhotoCursor.projection, q, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(BuildConfig.DEBUG) Log.d(TAG,"count:"+cursor.getCount());
        photoCursor = new PhotoCursor(cursor);
        mGroup = new PhotoGroup(photoCursor);
        mGroup.exec(getPartitionDistance(mapBounds));
        if(BuildConfig.DEBUG) Log.d(TAG,"group:"+mGroup.size());
        for(PhotoGroup.Group p:mGroup){
            mMap.addMarker(new MarkerOptions().position(p.getCenter()).title(String.valueOf(p.size())));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {
        photoCursor = null;
        mGroup.clear();
    }

}
