package net.mmho.photomap2

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import android.view.ViewGroup

internal class PhotoViewAdapter(fm: FragmentManager, private val group: PhotoGroup?)
        : FragmentPagerAdapter(fm) {

    override fun destroyItem(container: ViewGroup, position: Int, obj:Any) {
        val manager = (obj as Fragment).requireFragmentManager()
        val transaction = manager.beginTransaction()
        transaction.remove(obj)
        transaction.commit()
    }

    override fun getItem(i: Int): Fragment {
        val f = PhotoViewFragment()
        val b = Bundle()
        b.putLong(PhotoViewFragment.EXTRA_IMAGE_ID, getItemID(i))
        f.arguments = b
        return f
    }

    override fun getCount(): Int {
        return group?.size ?: 0
    }

    fun getItemID(i: Int): Long {
        return group?.get(i)?.photo_id ?: 0
    }

}
