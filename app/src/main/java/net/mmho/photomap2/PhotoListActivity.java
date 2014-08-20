package net.mmho.photomap2;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class PhotoListActivity extends Activity {

    final static String TAG_LIST="list";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        setContentView(R.layout.activity_photo_list);

        setProgress(0);
    }
}
