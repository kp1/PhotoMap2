package net.mmho.photomap2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PhotoViewActivity extends ActionBarActivity{

    public static final String EXTRA_GROUP = "photo_group";
    public static final String EXTRA_POSITION = "position";

    private PhotoViewAdapter adapter;
    private ViewPager pager;
    private ShareActionProvider shareActionProvider;
    private PhotoGroup group;

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
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showActionBar(true);
        ActionBar bar = getSupportActionBar();
        if(bar!=null) bar.addOnMenuVisibilityListener(menuVisibilityListener);

        group = bundle.getParcelable(EXTRA_GROUP);
        if(group.address!=null){
            setTitle(AddressUtil.getTitle(group.address,this));
        }
        int position = bundle.getInt(EXTRA_POSITION);
        adapter = new PhotoViewAdapter(getSupportFragmentManager(), group);

        pager = (ViewPager) findViewById(R.id.photo_pager);
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(onPageChangeListener);
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
        else hideActionBarDelayed();
    }

    private void hideActionBar(){
        ActionBar bar = getSupportActionBar();
        if(bar!=null){
            bar.hide();
        }
    }

    private void hideActionBarDelayed(){
        final long DELAY=3*1000;
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,DELAY);
    }


    final private ActionBar.OnMenuVisibilityListener menuVisibilityListener =
            new ActionBar.OnMenuVisibilityListener() {
                @Override
                public void onMenuVisibilityChanged(boolean isVisible) {
                    if(isVisible) showActionBar(false);
                    else hideActionBarDelayed();
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

    final private ViewPager.OnPageChangeListener onPageChangeListener =
            new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i2) {

                }

                @Override
                public void onPageSelected(int i) {
                    if(shareActionProvider!=null)
                        shareActionProvider.setShareIntent(setShareIntent(new Intent(),i));
                }

                @Override
                public void onPageScrollStateChanged(int i) {

                }
            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_view_menu,menu);
        MenuItem share = menu.findItem(R.id.share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(share);
        shareActionProvider.setShareIntent(setShareIntent(new Intent(),pager.getCurrentItem()));
        shareActionProvider.setSubUiVisibilityListener(new ShareActionProvider.SubUiVisibilityListener() {
            @Override
            public void onSubUiVisibilityChanged(boolean visible) {
                if(visible) showActionBar(false);
                else hideActionBarDelayed();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private Intent setShareIntent(Intent intent,int index){
        final long image_id = adapter.getItemID(index);
        final Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                String.valueOf(image_id));
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.setType("image/jpeg");
        return intent;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
        case R.id.map:
            intent = new Intent(this,PhotoMapActivity.class);
            setShareIntent(intent, pager.getCurrentItem());
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
