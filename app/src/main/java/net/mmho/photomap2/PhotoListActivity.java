package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class PhotoListActivity extends FragmentActivity {

    final static String TAG_LIST="list";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_list);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_LIST);
        if(fragment==null){
            fragment = new PhotoListFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_LIST);
            fragmentTransaction.commit();
        }

    }
}
