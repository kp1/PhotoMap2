package net.mmho.photomap2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class PhotoViewActivity extends FragmentActivity {

    private static final String TAG = "PhotoViewActivity";
    public static final String EXTRA_GROUP = "photo_group";
    public static final String EXTRA_POSITION = "position";

    private PhotoViewAdapter adapter;
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            setContentView(R.layout.fragment_photo_view);
            PhotoGroup group = bundle.getParcelable(EXTRA_GROUP);
            int position = bundle.getInt(EXTRA_POSITION);
            adapter = new PhotoViewAdapter(getSupportFragmentManager(), group);

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_view_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
        case R.id.share:
            final long image_id = adapter.getItemID(pager.getCurrentItem());
            final Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                   String.valueOf(image_id));
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/jpg");
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(Intent.EXTRA_STREAM,uri);
            startActivity(Intent.createChooser(intent,null));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
