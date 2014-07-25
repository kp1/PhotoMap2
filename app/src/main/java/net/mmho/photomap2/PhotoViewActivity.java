package net.mmho.photomap2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

public class PhotoViewActivity extends Activity {

    private static final String TAG = "PhotoViewActivity";
    private static final String TAG_PHOTO_VIEW="photo_view";
    public static final String EXTRA_GROUP = "photo_group";
    public static final String EXTRA_POSITION = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            if(BuildConfig.DEBUG) Log.d(TAG, "bundle is null.");
            finish();
        }

        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_PHOTO_VIEW);
        if(fragment==null){
            fragment = new PhotoViewFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_PHOTO_VIEW);
            fragmentTransaction.commit();
        }

    }
}
