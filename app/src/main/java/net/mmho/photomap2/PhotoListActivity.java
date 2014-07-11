package net.mmho.photomap2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class PhotoListActivity extends Activity{

    final static String TAG_LIST="list";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_list);

        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_LIST);
        if(fragment==null){
            fragment = new PhotoListFragment();
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(android.R.id.content,fragment,TAG_LIST);
            fragmentTransaction.commit();
        }

    }
}
