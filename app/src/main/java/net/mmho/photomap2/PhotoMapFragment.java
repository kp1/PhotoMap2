package net.mmho.photomap2;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PhotoMapFragment extends SupportMapFragment {
	final static String TAG="CustomMapFragment";
	final static int PARTITION_RATIO = 6;
    final static int PHOTO_CURSOR_LOADER = 0;
    final static int PHOTO_GROUP_LOADER = 1;

    private GoogleMap mMap;
    private LatLngBounds mapBounds;
    private PhotoCursor photoCursor;
    private PhotoGroupList mGroup;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mMap = getMap();
        mMap.setOnCameraChangeListener(photoMapCameraChangeListener);
        mMap.setOnMarkerClickListener(photoGroupClickListener);
        getLoaderManager().initLoader(PHOTO_CURSOR_LOADER, null, photoListLoaderCallback);
    }

    GoogleMap.OnCameraChangeListener photoMapCameraChangeListener=
        new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                if(BuildConfig.DEBUG)Log.d(TAG,position.toString());
                getLoaderManager().restartLoader(0, null,photoListLoaderCallback);
            }
        };

    GoogleMap.OnMarkerClickListener photoGroupClickListener =
        new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                for(PhotoGroup group:mGroup){
                    if(group.marker.equals(marker)){
                        if(BuildConfig.DEBUG) Log.d(TAG,"group:"+group.getArea());
                        Intent i = new Intent(getActivity(),ThumbnailActivity.class);
                        i.putExtra(ThumbnailActivity.EXTRA_GROUP,group);
                        startActivity(i);
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

    LoaderManager.LoaderCallbacks<Cursor> photoListLoaderCallback =
        new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                String q = QueryBuilder.createQuery(mapBounds);
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                return new CursorLoader(getActivity().getApplicationContext(),uri, PhotoCursor.projection, q, null, null);

            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                photoCursor = new PhotoCursor(cursor);
                getLoaderManager().restartLoader(PHOTO_GROUP_LOADER,null,photoGroupListLoaderCallbacks);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> objectLoader) {
                photoCursor = null;
                getLoaderManager().destroyLoader(PHOTO_GROUP_LOADER);
                mGroup.clear();
            }
        };

    LoaderManager.LoaderCallbacks<PhotoGroupList> photoGroupListLoaderCallbacks =
        new LoaderManager.LoaderCallbacks<PhotoGroupList>() {
            @Override
            public Loader<PhotoGroupList> onCreateLoader(int id, Bundle args) {
                return new PhotoGroupListLoader(getActivity().getApplicationContext(),new PhotoGroupList(photoCursor),getPartitionDistance(mapBounds),null);
            }

            @Override
            public void onLoadFinished(Loader<PhotoGroupList> loader, PhotoGroupList data) {
                if(mGroup==null || !mGroup.equals(data)){
                    mGroup = data;
                    mMap.clear();
                    for(PhotoGroup group:mGroup){
                        MarkerOptions ops = new MarkerOptions().position(group.getCenter());
                        ops.icon(BitmapDescriptorFactory.defaultMarker(PhotoGroup.getMarkerColor(group.size())));
                        group.marker = mMap.addMarker(ops);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<PhotoGroupList> loader) {

            }
        };

}
