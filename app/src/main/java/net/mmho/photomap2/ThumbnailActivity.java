package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class ThumbnailActivity extends FragmentActivity {

    private final static String TAG="ThumbnailActivity";
    private final static String TAG_THUMBNAIL="thumbnail";
    public static final String EXTRA_GROUP = "thumbnail_group";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            if(BuildConfig.DEBUG) Log.d(TAG, "bundle is null.");
            finish();
        }

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_THUMBNAIL);
        if(fragment==null){
            fragment = new ThumbnailFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_THUMBNAIL);
            fragmentTransaction.commit();
        }

    }
}
