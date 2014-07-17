package net.mmho.photomap2;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by kp on 14/07/17.
 */
public class PhotoGroupListLoader extends AsyncTaskLoader<PhotoGroupList> {

    private PhotoGroupList list;
    private float distance;

    public PhotoGroupListLoader(Context context,PhotoCursor cursor,float distance) {
        super(context);
        list = new PhotoGroupList(cursor);
        this.distance = distance;
        onContentChanged();
    }

    @Override
    public PhotoGroupList loadInBackground() {
        return list.exec(distance);
    }

    @Override
    protected void onStartLoading() {
        if(list.getDistance()==distance){
            deliverResult(list);
        }
        if(takeContentChanged()){
            forceLoad();
        }
    }
}
