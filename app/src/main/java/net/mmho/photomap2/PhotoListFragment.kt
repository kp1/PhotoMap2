package net.mmho.photomap2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.view.*
import android.widget.GridView
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.*

class PhotoListFragment : Fragment() {

    private var adapter: PhotoListAdapter? = null
    private var photoList: ArrayList<HashedPhoto>? = null
    private var newest = true
    private var distance_index: Int = 0

    // progress
    private var listener: ProgressChangeListener? = null

    // rxAndroid
    private var subscription: Subscription? = null
    private var subject: PublishSubject<Int>? = null
    private var permission_granted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        setHasOptionsMenu(true)

        photoList = ArrayList<HashedPhoto>()
        adapter = PhotoListAdapter(activity, R.layout.layout_photo_card, ArrayList<PhotoGroup>())
        when(savedInstanceState) {
            null -> {
                distance_index = DistanceActionProvider.initialIndex()
            }
            else -> {
                distance_index = savedInstanceState.getInt("DISTANCE")
                activity.title = savedInstanceState.getString("title")
            }
        }
        subject = PublishSubject.create<Int>()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 23
            && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PhotoListActivity.PERMISSIONS_REQUEST)
            } else {
                PermissionUtils.requestPermission(view, context)
            }
        } else {
            grantedPermission(true)
        }
    }

    override fun onStart() {
        super.onStart()
        if (subscription == null)
            subscription = subject?.switchMap { distance -> this@PhotoListFragment.groupObservable(distance) }?.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unsubscribe()
        subscription = null
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.photo_list_menu, menu)

        val distance = menu!!.findItem(R.id.distance)
        val distanceActionProvider = MenuItemCompat.getActionProvider(distance) as DistanceActionProvider
        distanceActionProvider.setDistanceIndex(distance_index)
        distanceActionProvider.setOnDistanceChangeListener(object : DistanceActionProvider.OnDistanceChangeListener {
            override fun onDistanceChange(index: Int) {
                distance_index = index
                subject?.onNext(index)
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.oldest -> {
                newest = false
                loaderManager.restartLoader(CURSOR_LOADER_ID, Bundle(), photoCursorCallbacks)
            }
            R.id.newest -> {
                newest = true
                loaderManager.restartLoader(CURSOR_LOADER_ID, Bundle(), photoCursorCallbacks)
            }
            R.id.about -> {
                val i = Intent(activity, AboutActivity::class.java)
                startActivity(i)
            }
            else -> {}
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        when (permission_granted){
            true ->{
                menu.findItem(R.id.newest).isEnabled = !newest
                menu.findItem(R.id.oldest).isEnabled = newest
                menu.findItem(R.id.distance).isEnabled = true
            }
            else -> {
                menu.findItem(R.id.newest).isEnabled = false
                menu.findItem(R.id.oldest).isEnabled = false
                menu.findItem(R.id.distance).isEnabled = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_thumbnail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // photo list
        val list = view.findViewById(R.id.thumbnail_grid) as GridView
        list.adapter = adapter
        list.setOnItemClickListener { p, v, position, id ->
            val group = adapter!!.getItem(position)
            val intent: Intent
            if (group.size == 1) {
                intent = Intent(activity, PhotoViewActivity::class.java)
                intent.putExtra(PhotoViewActivity.EXTRA_GROUP, group as Parcelable)
            } else {
                intent = Intent(activity, ThumbnailActivity::class.java)
                intent.putExtra(ThumbnailActivity.EXTRA_GROUP, group as Parcelable)
            }
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("DISTANCE", distance_index)
        outState.putString("title", activity.title.toString())
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (activity !is ProgressChangeListener) {
            throw RuntimeException(activity.localClassName + " must implement ProgressChangeListener")
        }
        listener = activity as ProgressChangeListener
    }

    private val photoCursorCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
            val q = QueryBuilder.createQuery()  // all list
            val o = if (newest) QueryBuilder.sortDateNewest() else QueryBuilder.sortDateOldest()
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            return CursorLoader(activity, uri, PhotoCursor.projection, q, null, o)
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
            photoList = PhotoCursor(data).hashedPhotoList
            subject?.onNext(distance_index)
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
        }
    }

    private var progress: Int = 0
    private var group_count: Int = 0

    private fun groupObservable(distance: Int): Observable<PhotoGroup> {
        return Observable.from(photoList)
            .subscribeOn(Schedulers.newThread())
            .groupBy { hash -> hash.hash.toBase32().substring(0, DistanceActionProvider.getDistance(distance)) }
            .doOnNext { group_count++ }
            .concatMap {
                group -> group.map { p -> PhotoGroup(p) }
                .reduce { hashedPhotos, o -> hashedPhotos.append(o) } }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                adapter?.clear()
                listener?.showProgress(0)
                progress = 0
                group_count = 0
            }
            .doOnNext { g ->
                listener?.showProgress(++progress * 10000 / group_count)
                adapter?.add(g)
            }
            .doOnCompleted { listener?.endProgress() }
    }

    fun grantedPermission(granted: Boolean) {
        if (granted) {
            loaderManager.initLoader(CURSOR_LOADER_ID, Bundle(), photoCursorCallbacks)
        } else {
            val v = view
            if (v != null) PermissionUtils.requestPermission(v, context)
        }
        permission_granted = granted
    }

    companion object {
        private val CURSOR_LOADER_ID = 0
    }
}
