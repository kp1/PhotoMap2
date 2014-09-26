package net.mmho.photomap2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PhotoViewActivity extends ActionBarActivity {

    public static final String EXTRA_GROUP = "photo_group";
    public static final String EXTRA_POSITION = "position";

    private PhotoViewAdapter adapter;
    private ViewPager pager;
    private long HIDE_DELAY=3*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();

        if(bundle==null){
            finish();
            return;
        }

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.fragment_photo_view);

        showActionBar(true);
        ActionBar bar = getSupportActionBar();
        if(bar!=null) bar.addOnMenuVisibilityListener(menuVisibilityListener);

        PhotoGroup group = bundle.getParcelable(EXTRA_GROUP);
        if(group.address!=null){
            setTitle(AddressUtil.getTitle(group.address,this));
        }
        int position = bundle.getInt(EXTRA_POSITION);
        adapter = new PhotoViewAdapter(getSupportFragmentManager(), group);

        pager = (ViewPager) findViewById(R.id.photo_pager);
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);
        pager.setPageMargin(30);
        pager.setOnClickListener(onClickListener);
    }

    final private Handler handler = new Handler();
    final private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            hideActionBar();
        }
    };

    private void showActionBar(boolean hide) {
        ActionBar bar = getSupportActionBar();
        if(bar!=null) bar.show();
        if(!hide) handler.removeCallbacks(runnable);
        else hideActionBarDelayed(HIDE_DELAY);
    }

    private void hideActionBar(){
        ActionBar bar = getSupportActionBar();
        if(bar!=null){
            bar.hide();
        }
    }

    private void hideActionBarDelayed(long delay){
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,delay);
    }


    final private ActionBar.OnMenuVisibilityListener menuVisibilityListener =
            new ActionBar.OnMenuVisibilityListener() {
                @Override
                public void onMenuVisibilityChanged(boolean isVisible) {
                    if(isVisible) showActionBar(false);
                    else hideActionBarDelayed(HIDE_DELAY);
                }
            };

    final private View.OnClickListener onClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActionBar bar = getSupportActionBar();
                    if(bar!=null){
                        if(bar.isShowing()) hideActionBar();
                        else showActionBar(true);
                    }
                }
            };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_view_menu,menu);
        return true;
    }

    private void setUri(Intent intent){
        final long image_id = adapter.getItemID(pager.getCurrentItem());
        final Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                String.valueOf(image_id));
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
        case R.id.share:
            intent = new Intent();
            setUri(intent);
            startActivity(Intent.createChooser(intent,null));
            return true;
        case R.id.map:
            intent = new Intent(this,PhotoMapActivity.class);
            setUri(intent);
            startActivity(intent);
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
