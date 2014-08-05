package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

public class PhotoListActivity extends ActionBarActivity {

    final static String TAG_LIST="list";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        setContentView(R.layout.activity_photo_list);

        setProgress(0);
    }
}
