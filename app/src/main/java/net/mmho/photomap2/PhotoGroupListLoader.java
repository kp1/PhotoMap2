package net.mmho.photomap2;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.util.Log;

public class PhotoGroupListLoader extends AsyncTaskLoader<PhotoGroupList> {

    private static final String TAG = "PhotoGroupListLoader";
    private PhotoGroupList list;
    private float distance;
    private Handler handler;
    private CancellationSignal signal;

    public PhotoGroupListLoader(Context context,PhotoGroupList list,float distance,Handler handler) {
        super(context);
        this.list = list;
        this.distance = distance;
        this.handler = handler;
        signal = null;
        onContentChanged();
    }

    @Override
    public PhotoGroupList loadInBackground() {
        signal = new CancellationSignal();
        try {
            list.exec(distance, handler, signal);
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
    public boolean cancelLoad() {
        return super.cancelLoad();
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
        list = null;
        super.onReset();
    }
}
