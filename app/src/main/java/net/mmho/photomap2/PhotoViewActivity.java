package net.mmho.photomap2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class PhotoViewActivity extends FragmentActivity {

    private static final String TAG = "PhotoViewActivity";
    public static final String EXTRA_GROUP = "photo_group";
    public static final String EXTRA_POSITION = "position";

    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            setContentView(R.layout.fragment_photo_view);
            PhotoGroup group = bundle.getParcelable(EXTRA_GROUP);
            int position = bundle.getInt(EXTRA_POSITION);
            PhotoViewAdapter adapter = new PhotoViewAdapter(getSupportFragmentManager(), group);

            pager = (ViewPager) findViewById(R.id.photo_pager);
            pager.setAdapter(adapter);
            pager.setCurrentItem(position);

        }
        else {
            if (BuildConfig.DEBUG) Log.d(TAG, "bundle is null.");
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        Bundle b = new Bundle();
        b.putInt(EXTRA_POSITION,pager.getCurrentItem());
        i.putExtras(b);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }
}
