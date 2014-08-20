package net.mmho.photomap2;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ThumbnailActivity extends Activity {

    private final static String TAG="ThumbnailActivity";
    private final static String TAG_THUMBNAIL="thumbnail";
    public static final String EXTRA_GROUP = "thumbnail_group";
    private ThumbnailFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            if(BuildConfig.DEBUG) Log.d(TAG, "bundle is null.");
            finish();
        }

        fragment = (ThumbnailFragment) getFragmentManager().findFragmentByTag(TAG_THUMBNAIL);
        if(fragment==null){
            fragment = new ThumbnailFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_THUMBNAIL);
            fragmentTransaction.commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {
            Bundle b = data.getExtras();
            if (b != null) fragment.setPosition(b.getInt(PhotoViewActivity.EXTRA_POSITION));
        }
    }
}
