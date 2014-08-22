package net.mmho.photomap2;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.OperationCanceledException;

public class PhotoGroupListLoader extends AsyncTaskLoader<PhotoGroupList> {

    private PhotoGroupList list;
    private float distance;
    private Handler handler;
    private CancellationSignal signal;
    private boolean geocode;

    public PhotoGroupListLoader(Context context,Cursor cursor,float distance,boolean geocode,Handler handler) {
        super(context);
        this.list = new PhotoGroupList(new PhotoCursor(cursor));
        this.distance = distance;
        this.geocode = geocode;
        this.handler = handler;
        signal = null;

        onContentChanged();
    }

    @Override
    public PhotoGroupList loadInBackground() {
        signal = new CancellationSignal();
        try {
            list.exec(distance,geocode,getContext(), handler, signal);
        }
        catch (OperationCanceledException e){
            // do nothing
        }
        signal = null;
        return list;
    }

    @Override
    protected void onStartLoading() {
        if(list.getFinished() && list.getDistance()==distance){
            deliverResult(list);
        }
        else if(takeContentChanged()){
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        if(signal!=null){
            signal.cancel();
        }
        super.onReset();
    }
}
