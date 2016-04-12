package net.mmho.photomap2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

internal class PhotoViewAdapter(fm: FragmentManager, private val group: PhotoGroup)
        : FragmentPagerAdapter(fm) {

    override fun getItem(i: Int): Fragment {
        val f = PhotoViewFragment()
        val b = Bundle()
        b.putLong(PhotoViewFragment.EXTRA_IMAGE_ID, group[i].photoId)
        f.arguments = b
        return f
    }

    override fun getCount(): Int {
        return group.size
    }

    fun getItemID(i: Int): Long {
        return group[i].photoId
    }

}
