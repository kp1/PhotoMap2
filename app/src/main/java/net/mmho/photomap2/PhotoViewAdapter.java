package net.mmho.photomap2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PhotoViewAdapter extends FragmentPagerAdapter {

    private PhotoGroup group;

    public PhotoViewAdapter(FragmentManager fm,PhotoGroup group) {
        super(fm);
        this.group = group;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment f = new PhotoViewFragment();
        Bundle b = new Bundle();
        b.putLong(PhotoViewFragment.EXTRA_IMAGE_ID,group.get(i).getPhotoId());
        f.setArguments(b);
        return f;
    }

    @Override
    public int getCount() {
        return group.size();
    }

    public long getItemID(int i){
        return group.get(i).getPhotoId();
    }

}
