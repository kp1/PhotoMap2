package net.mmho.photomap2;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoMapFragment extends MapFragment {
	final static int PARTITION_RATIO = 6;
    final static int PHOTO_CURSOR_LOADER = 0;
    final static int PHOTO_GROUP_LOADER = 1;

    final public static String EXTRA_GROUP="group";
    final private static float DEFAULT_ZOOM = 14;

    private GoogleMap mMap;
    private LatLngBounds mapBounds;
    private Cursor photoCursor;
    private PhotoGroupList mGroup;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

    private LatLngBounds expandLatLngBounds(LatLngBounds bounds,double percentile){
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
                    Toast.makeText(getActivity(),getString(R.string.no_search_query), Toast.LENGTH_LONG).show();
                    getActivity().finish();
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
            if(getView()!=null) {
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        mMap.moveCamera(update);
                    }
                });
            }
            mMap.setOnCameraChangeListener(photoMapCameraChangeListener);
            mMap.setOnMarkerClickListener(photoGroupClickListener);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            getLoaderManager().initLoader(PHOTO_CURSOR_LOADER, null, photoListLoaderCallback);
        }
    }

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
                String o = QueryBuilder.sortDateNewest();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                return new CursorLoader(getActivity(),uri, PhotoCursor.projection, q, null, o);

            }

            @Override
            public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                photoCursor = cursor;
                if(photoCursor.getCount()!=0) {
                    getLoaderManager().destroyLoader(PHOTO_GROUP_LOADER);
                    getLoaderManager().restartLoader(PHOTO_GROUP_LOADER, null, photoGroupListLoaderCallbacks);
                }
                else{
                    mMap.clear();
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
                        photoCursor,getPartitionDistance(mapBounds),false,null);
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
