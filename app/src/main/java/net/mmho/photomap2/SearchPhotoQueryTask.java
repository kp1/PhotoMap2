package net.mmho.photomap2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

public class SearchPhotoQueryTask extends AsyncTask<LatLngBounds,Void,Cursor>{

	final static String TAG="SearchPhotoQueryTask";
	static Cursor mCursor;
	
	@Override
	protected Cursor doInBackground(LatLngBounds... params) {
		LatLngBounds bounds = params[0];
		return searchPhoto(bounds);
//		return null;
	}
	
	@Override
	protected void onPostExecute(Cursor result) {
		if(!mCursor.isClosed()) mCursor.close();
		super.onPostExecute(result);
	}
	
	private Cursor searchPhoto(LatLngBounds bounds) {
		String q = createQueryString(bounds);
		double latitude = bounds.southwest.latitude+(bounds.northeast.latitude-bounds.southwest.latitude)/2;
		double longitude = bounds.southwest.longitude+(bounds.northeast.longitude-bounds.southwest.longitude)/2;
		String o = createQueryOrder(latitude,longitude);
    	Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    	
    	final String[] projection = new String[]{
    		MediaStore.Images.Media._ID,
    		MediaStore.Images.Media.DATA,
    		MediaStore.Images.Media.DISPLAY_NAME,
    		MediaStore.Images.Media.LATITUDE,
    		MediaStore.Images.Media.LONGITUDE,
    		MediaStore.Images.Media.DATE_TAKEN,
    		MediaStore.Images.Media.ORIENTATION,
    	};

    	mCursor = PhotoMapActivity.getContext().getContentResolver().query(uri, projection, q, null, o);
		return mCursor;
	}

	private String createQueryOrder(double latitude,double longitude) {
        StringBuilder b = new StringBuilder();
        b.append("((latitude-(").append(latitude).append("))*(latitude-(").append(latitude).append(")))+");
        b.append("((longitude-(").append(longitude).append("))*(longitude-(").append(longitude).append("))) ");
        b.append("asc limit 50");
    	return new String(b);
	}

	private String createQueryString(LatLngBounds bounds) {
		LatLng start = bounds.southwest;
		LatLng end = bounds.northeast;
        StringBuilder b = new StringBuilder();
        b.append("(latitude between ").append(start.latitude).append(" and ").append(end.latitude).append(")");
        b.append(" and (longitude between ").append(start.longitude).append(" and ").append(end.longitude).append(")");
		return new String(b);
	}
	
}
