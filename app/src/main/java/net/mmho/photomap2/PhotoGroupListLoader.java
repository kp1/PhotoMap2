package net.mmho.photomap2;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

public class PhotoGroupListLoader extends AsyncTaskLoader<PhotoGroupList> {

    private PhotoGroupList list;
    private float distance;

    public PhotoGroupListLoader(Context context,PhotoGroupList list,float distance) {
        super(context);
        this.list = list;
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
