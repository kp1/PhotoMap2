package net.mmho.photomap2;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

import java.util.concurrent.CancellationException;

public class PhotoGroupListLoader extends AsyncTaskLoader<PhotoGroupList> {

    private PhotoGroupList list;
    private PhotoCursor cursor;
    private int distance;
    private Handler handler;
    private boolean geocode;
    private boolean exec;

    public PhotoGroupListLoader(Context context,PhotoGroupList list,PhotoCursor cursor,int distance,boolean geocode,Handler handler) {
        super(context);
        this.list = list;
        this.cursor = cursor;
        this.distance = distance;
        this.geocode = geocode;
        this.handler = handler;

        onContentChanged();
    }

    @Override
    public PhotoGroupList loadInBackground() {
        exec = true;
        try {
            list.exec(cursor,distance,geocode,getContext(), handler);
        }
        catch (CancellationException e){
            // do nothing
        }
        exec = false;
        return list;
    }

    @Override
    protected void onStartLoading() {
        if(list.isFinished() && list.getDistance()==distance){
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
        list.cancel();
        while(exec){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        super.onReset();
    }
}
