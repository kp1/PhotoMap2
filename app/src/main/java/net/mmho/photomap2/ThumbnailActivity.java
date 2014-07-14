package net.mmho.photomap2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

public class ThumbnailActivity extends Activity {

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

        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_THUMBNAIL);
        if(fragment==null){
            fragment = new ThumbnailFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_THUMBNAIL);
            fragmentTransaction.commit();
        }

    }
}
