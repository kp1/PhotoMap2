package net.mmho.photomap2;

import android.app.Activity;
import android.os.Bundle;

public class PhotoListActivity extends Activity{

    private PhotoListFragment listFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_list);
        listFragment = (PhotoListFragment)getFragmentManager().findFragmentById(R.id.photo_list);


    }
}
