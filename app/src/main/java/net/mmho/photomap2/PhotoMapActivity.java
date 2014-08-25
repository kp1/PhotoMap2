package net.mmho.photomap2;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class PhotoMapActivity extends Activity{

    private static final String TAG_MAP = "map";

    private Dialog dialog = null;

    private final Handler cancelHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            finish();
        }
    };

    private final DialogInterface.OnCancelListener onCancelListener = new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            cancelHandler.sendEmptyMessage(0);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(result == ConnectionResult.SUCCESS){
            PhotoMapFragment fragment = (PhotoMapFragment) getFragmentManager().findFragmentByTag(TAG_MAP);
            if(fragment==null){
                fragment = new PhotoMapFragment();
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.add(android.R.id.content,fragment,TAG_MAP);
                fragmentTransaction.commit();
            }
        }
        else if(GooglePlayServicesUtil.isUserRecoverableError(result)){
            dialog = GooglePlayServicesUtil.getErrorDialog(result,this,1,onCancelListener);
            dialog.show();
        }
        else{
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(dialog!=null && dialog.isShowing()) dialog.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
    }
}
