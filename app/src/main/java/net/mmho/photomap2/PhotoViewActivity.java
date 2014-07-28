package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class PhotoViewActivity extends FragmentActivity {

    private static final String TAG = "PhotoViewActivity";
    public static final String EXTRA_GROUP = "photo_group";
    public static final String EXTRA_POSITION = "position";

    private PhotoGroup group;
    private int position;
    private PhotoViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            if(BuildConfig.DEBUG) Log.d(TAG, "bundle is null.");
            finish();
        }

        setContentView(R.layout.fragment_photo_view);

        group = bundle.getParcelable(EXTRA_GROUP);

        group = bundle.getParcelable(PhotoViewActivity.EXTRA_GROUP);
        position = bundle.getInt(PhotoViewActivity.EXTRA_POSITION);
        adapter = new PhotoViewAdapter(getSupportFragmentManager(),group);

        ViewPager pager = (ViewPager)findViewById(R.id.photo_pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);

    }
}
