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
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PhotoMapFragment extends MapFragment implements LoaderManager.LoaderCallbacks<Cursor>{
	final static String TAG="CustomMapFragment";
	final static int PARTITION_RATIO = 10;

    private GoogleMap mMap;
    private LatLngBounds mapBounds;
    private PhotoCursor photoCursor;
    private PhotoGroup mGroup;
    private int widthPix;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        ViewTreeObserver observer = getView().getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = getView().getWidth();
                int height = getView().getHeight();
                widthPix = Math.min(width, height);
            }
        });


        mMap = getMap();
        mMap.setOnCameraChangeListener(photoMapCameraChangeListener);
        mMap.setOnMarkerClickListener(photoGroupClickListener);
        getLoaderManager().initLoader(0, null, this);
    }

    GoogleMap.OnCameraChangeListener photoMapCameraChangeListener=
        new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                mMap.clear();
                if(BuildConfig.DEBUG)Log.d(TAG,position.toString());
                getLoaderManager().restartLoader(0, null, PhotoMapFragment.this);
            }
        };

    GoogleMap.OnMarkerClickListener photoGroupClickListener =
        new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(BuildConfig.DEBUG) Log.d(TAG,"onMarkerClick");
                for(PhotoGroup.Group p:mGroup){
                    if(p.marker.equals(marker)){
                        if(BuildConfig.DEBUG) Log.d(TAG,"group:"+p.getArea());
                        //stop CameraChangeListener
                        mMap.setOnCameraChangeListener(null);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(p.getArea(), widthPix/10), animateCameraCallback);
                        break;
                    }
                }
                return true;
            }
        };

    GoogleMap.CancelableCallback animateCameraCallback =
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
            p.marker = mMap.addMarker(new MarkerOptions().position(p.getCenter()).title(String.valueOf(p.size())));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {
        photoCursor = null;
        mGroup.clear();
    }

}
