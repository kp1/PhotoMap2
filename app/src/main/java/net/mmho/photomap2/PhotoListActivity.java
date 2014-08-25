package net.mmho.photomap2;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;

public class PhotoListActivity extends Activity {

    final static String TAG_LIST="list";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);

        ActionBar bar = getActionBar();
        if(bar!=null) bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_LIST);
        if(fragment==null){
            fragment = new PhotoListFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_LIST);
            fragmentTransaction.commit();
        }
    }
}
