package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class PhotoViewAdapter extends FragmentPagerAdapter {

    private static final String TAG = "PhotoViewAdapter";
    private PhotoGroup group;

    public PhotoViewAdapter(FragmentManager fm,PhotoGroup group) {
        super(fm);
        this.group = group;
    }

    @Override
    public Fragment getItem(int i) {
        if(BuildConfig.DEBUG) Log.d(TAG, "getItem:"+i);
        Fragment f = new PhotoViewFragment();
        Bundle b = new Bundle();
        b.putLong(PhotoViewFragment.EXTRA_IMAGE_ID,group.getID(i));
        f.setArguments(b);
        return f;
    }

    @Override
    public int getCount() {
        return group.getIDList().size();
    }
}
