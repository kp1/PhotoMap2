package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class PhotoViewActivity extends FragmentActivity {

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

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_PHOTO_VIEW);
        if(fragment==null){
            fragment = new PhotoViewFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_PHOTO_VIEW);
            fragmentTransaction.commit();
        }

    }
}
