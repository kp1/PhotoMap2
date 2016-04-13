package net.mmho.photomap2

import android.content.Context
import android.support.v4.view.ActionProvider
import android.view.MenuItem
import android.view.SubMenu
import android.view.View

class DistanceActionProvider(context: Context) : ActionProvider(context), MenuItem.OnMenuItemClickListener {
    private var onDistanceChangeListener: OnDistanceChangeListener? = null
    private var selected: Int = 0

    private fun pretty(position: Int): String {
        var meter = meters[position]
        var unit = "m"
        if (meter >= 1000) {
            unit = "km"
            meter /= 1000
        }
        return context.getString(R.string.about) + meter.toString() + unit
    }


    init {
        selected = INITIAL_INDEX
    }

    fun setDistanceIndex(index: Int) {
        selected = index
    }

    override fun onCreateActionView(): View? {
        return null
    }

    override fun hasSubMenu(): Boolean {
        return true
    }

    override fun onPrepareSubMenu(subMenu: SubMenu?) {
        subMenu!!.clear()

        for (i in distance.indices) {
            val s = subMenu.add(0, i, i, pretty(i))
            s.setOnMenuItemClickListener(this)
            if (i == selected) s.isEnabled = false
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        selected = item.itemId
        onDistanceChangeListener?.onDistanceChange(selected)
        return false
    }

    interface OnDistanceChangeListener {
        fun onDistanceChange(index: Int)
    }

    fun setOnDistanceChangeListener(changeListener: OnDistanceChangeListener) {
        onDistanceChangeListener = changeListener
    }

    companion object {

        private val INITIAL_INDEX = 1

        private val distance = intArrayOf(7, 6, 5, 4, 3)
        private val meters = intArrayOf(150, 600, 5000, 20000, 150000)

        fun initialIndex(): Int {
            return INITIAL_INDEX
        }

        fun getDistance(index: Int): Int {
            return distance[index]
        }
    }

}
