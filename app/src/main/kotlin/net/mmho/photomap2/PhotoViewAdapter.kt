package net.mmho.photomap2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup

internal class PhotoViewAdapter(fm: FragmentManager, private val group: PhotoGroup)
        : FragmentPagerAdapter(fm) {

    override fun destroyItem(container: ViewGroup?, position: Int, obj:Any) {
        var manager = (obj as Fragment).fragmentManager;
        var transaction = manager.beginTransaction();
        transaction.remove(obj)
        transaction.commit()
    }

    override fun getItem(i: Int): Fragment {
        val f = PhotoViewFragment()
        val b = Bundle()
        b.putLong(PhotoViewFragment.EXTRA_IMAGE_ID, group[i].photo_id)
        f.arguments = b
        return f
    }

    override fun getCount(): Int {
        return group.size
    }

    fun getItemID(i: Int): Long {
        return group[i].photo_id
    }

}
