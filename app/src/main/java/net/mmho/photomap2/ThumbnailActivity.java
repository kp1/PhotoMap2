package net.mmho.photomap2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class ThumbnailActivity extends ActionBarActivity {

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

        setContentView(R.layout.activity_thumbnail_list);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        fragment = (ThumbnailFragment) getSupportFragmentManager().findFragmentById(R.id.list);
        fragment.setList((PhotoGroup) bundle.getParcelable(EXTRA_GROUP));

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
