package net.mmho.photomap2

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.*
import android.widget.AdapterView
import android.widget.GridView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.layout_thumbnail.view.*

class ThumbnailFragment : Fragment() {

    private var group: PhotoGroup? = null
    private var list: GridView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
    }

    fun setList(g: PhotoGroup) {
        group = g
        val adapter = ThumbnailAdapter(requireActivity(), R.layout.adapter_thumbnail, group as PhotoGroup)
        list?.adapter = adapter
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Observable
            .just(group)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { g -> g?.resolveAddress(requireContext())}
            .doOnComplete { requireActivity().title = group?.title }
            .subscribe()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val parent = inflater.inflate(R.layout.fragment_thumbnail, container, false)
        list = parent.thumbnail_grid
        list?.onItemClickListener = clickListener
        return parent
    }

    fun setPosition(position: Int) {
        list?.setSelection(position)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.thumbnail_manu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.map -> {
                val i = Intent(activity, PhotoMapActivity::class.java)
                i.putExtra(PhotoMapFragment.EXTRA_GROUP, group as Parcelable?)
                startActivity(i)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val position = data?.getIntExtra(PhotoViewActivity.EXTRA_POSITION,0) ?: 0
        setPosition(position)
    }

    private val clickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        val i = Intent(activity, PhotoViewActivity::class.java)
        i.putExtra(PhotoViewActivity.EXTRA_GROUP, group as Parcelable?)
        i.putExtra(PhotoViewActivity.EXTRA_POSITION, position)
        startActivityForResult(i, 0)
    }
}
