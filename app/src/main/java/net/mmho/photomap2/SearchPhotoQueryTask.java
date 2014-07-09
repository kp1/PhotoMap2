package net.mmho.photomap2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

public class SearchPhotoQueryTask extends AsyncTask<LatLngBounds,Void,PhotoCursor>{

	final static String TAG="SearchPhotoQueryTask";

	@Override
	protected PhotoCursor doInBackground(LatLngBounds... params) {
		LatLngBounds bounds = params[0];
		return searchPhoto(bounds);
//		return null;
	}
	
	@Override
	protected void onPostExecute(PhotoCursor result) {
//		if(!mCursor.isClosed()) mCursor.close();
		super.onPostExecute(result);
	}
	
	private PhotoCursor searchPhoto(LatLngBounds bounds) {
		String q = createQueryString(bounds);
        LatLng c = bounds.getCenter();
		String o = createQueryOrder(c.latitude,c.longitude);
    	Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    	
    	return new PhotoCursor(PhotoMapActivity.getContext().getContentResolver().query(uri, PhotoCursor.projection, q, null, o));
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
