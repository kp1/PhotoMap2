package net.mmho.photomap2

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.SearchRecentSuggestions
import android.support.v4.app.ActivityCompat
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.*
import java.util.regex.Pattern


class PhotoMapFragment : SupportMapFragment() {

    private var googleMap: GoogleMap? = null
    private var searchMenuItem: MenuItem? = null
    private lateinit var actionBar: ActionBar
    private var photoList: ArrayList<HashedPhoto>? = null
    private var groupList: ArrayList<PhotoGroup>? = null

    private var listener: ProgressChangeListener? = null


    private lateinit var subject: PublishSubject<Int>
    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setHasOptionsMenu(true)
        photoList = ArrayList<HashedPhoto>()
        groupList = ArrayList<PhotoGroup>()
        subject = PublishSubject.create<Int>()
    }

    override fun onStart() {
        super.onStart()
        if (subscription == null) {
            subscription = subject.onBackpressureLatest().switchMap { this.groupObservable(it) }.subscribe()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscription?.unsubscribe()
        subscription = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (activity !is ProgressChangeListener) {
            throw RuntimeException(activity.localClassName + " must implement net.mmho.photomap2.ProgressChangeListener")
        }
        listener = activity as ProgressChangeListener?
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val position = data.extras.getParcelable<LatLng>("location")
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.photo_map_menu, menu)

        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = MenuItemCompat.getActionView(menu.findItem(R.id.search)) as SearchView
        searchView.setOnQueryTextListener(onQueryTextListener)
        searchMenuItem = menu.findItem(R.id.search)
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, actionExpandListener)
        searchView.setOnQueryTextFocusChangeListener { v, hasFocus -> if (!hasFocus) MenuItemCompat.collapseActionView(searchMenuItem) }
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        searchView.isQueryRefinementEnabled = true

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.clear_history -> {
                val suggestions = SearchRecentSuggestions(activity,
                    MapSuggestionProvider.AUTHORITY,
                    MapSuggestionProvider.MODE)
                suggestions.clearHistory()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val actionExpandListener = object : MenuItemCompat.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
            showActionBar(false)
            return true
        }

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
            hideActionBarDelayed()
            return true
        }
    }


    private val onQueryTextListener = object : SearchView.OnQueryTextListener {

        override fun onQueryTextSubmit(query: String): Boolean {
            MenuItemCompat.collapseActionView(searchMenuItem)
            requestQuery(query)
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            return false
        }
    }


    private fun requestQuery(query: String) {
        val intent = Intent(activity, PhotoMapActivity::class.java)
        intent.action = Intent.ACTION_SEARCH
        intent.putExtra(SearchManager.QUERY, query)
        startActivity(intent)
    }

    private fun expandLatLngBounds(bounds: LatLngBounds, percentile: Double): LatLngBounds {
        val lat_distance = (bounds.northeast.latitude - bounds.southwest.latitude) * ((percentile - 1.0) / 2)
        val lng_distance = (bounds.northeast.longitude - bounds.southwest.longitude) * ((percentile - 1.0) / 2)
        val northeast = LatLng(bounds.northeast.latitude + lat_distance, bounds.northeast.longitude + lng_distance)
        val southwest = LatLng(bounds.southwest.latitude - lat_distance, bounds.southwest.longitude - lng_distance)
        return LatLngBounds(southwest, northeast)
    }

    private fun handleIntent(intent: Intent): CameraUpdate? {
        when(intent.action){
            Intent.ACTION_VIEW ->{
                val uri = intent.data
                if (uri.scheme == "geo") {
                    val position = uri.toString()
                    val pattern = Pattern.compile("(-?\\d+.\\d+),(-?\\d+.\\d+)(\\?([zq])=(.*))?")
                    val matcher = pattern.matcher(position)
                    if (matcher.find() && matcher.groupCount() >= 2) {
                        val latitude = matcher.group(1).toDouble()
                        val longitude = matcher.group(2).toDouble()
                        var zoom = DEFAULT_ZOOM

                        if (matcher.groupCount() == 5 && matcher.group(4) != null) {
                            if (matcher.group(4) == "z") {
                                zoom = Integer.parseInt(matcher.group(5)).toFloat()
                            } else if (matcher.group(4) == "q") {
                                requestQuery(matcher.group(5))
                                return null
                            }
                        }
                        return CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom)
                    }
                }
            }
            Intent.ACTION_SEND ->{
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                val projection = arrayOf(MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE)
                val c = PhotoCursor(MediaStore.Images.Media.query(activity.contentResolver, uri, projection,
                        QueryBuilder.createQuery(), null, null))
                if (c.count == 0) {
                    Toast.makeText(activity, getString(R.string.no_position_data), Toast.LENGTH_LONG).show()
                    activity.finish()
                    return null
                }
                c.moveToFirst()
                val position = c.location
                c.close()
                return CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM)
            }
            else ->{
                val bundle = intent.extras
                val group = bundle.getParcelable<PhotoGroup>(EXTRA_GROUP)
                if (group != null) {
                    return CameraUpdateFactory.newLatLngBounds(expandLatLngBounds(group.hash.bounds, 1.2), 0)
                }
            }
        }
        return null
    }

    private fun initMap() {

        if (googleMap != null) {
            loaderManager.initLoader(PHOTO_CURSOR_LOADER, Bundle(), photoListLoaderCallback)
            return
        }

        getMapAsync { map ->
            googleMap = map

            val update = this@PhotoMapFragment.handleIntent(this@PhotoMapFragment.activity.intent)
            if(update!=null) this@PhotoMapFragment.view?.post{ googleMap?.moveCamera(update) }

            googleMap?.setOnCameraChangeListener(photoMapCameraChangeListener)
            googleMap?.setOnMarkerClickListener { marker ->
                Observable.from(groupList)
                    .filter { g -> g.marker == marker }
                    .first()
                    .subscribe { g ->
                        val i: Intent
                        when(g.size) {
                            1 -> {
                                i = Intent(this@PhotoMapFragment.activity, PhotoViewActivity::class.java)
                                i.putExtra(PhotoViewActivity.EXTRA_GROUP, g as Parcelable)
                            }
                            else -> {
                                i = Intent(this@PhotoMapFragment.activity, ThumbnailActivity::class.java)
                                i.putExtra(ThumbnailActivity.EXTRA_GROUP, g as Parcelable)
                            }
                        }
                        this@PhotoMapFragment.startActivity(i)
                    }
                true
            }
            googleMap?.setOnMapClickListener {
                if (actionBar.isShowing)
                    this@PhotoMapFragment.hideActionBar()
                else
                    this@PhotoMapFragment.showActionBar(true)
            }
            googleMap?.uiSettings?.isZoomControlsEnabled = false
            this@PhotoMapFragment.loaderManager.initLoader(PHOTO_CURSOR_LOADER, Bundle(), photoListLoaderCallback)
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PhotoMapActivity.PERMISSIONS_REQUEST)
            } else {
                val v = view
                if (v != null) PermissionUtils.requestPermission(v, context)
            }
        } else {
            initMap()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(activity !is AppCompatActivity) throw RuntimeException("activity must extends AppCompatActivity")
        actionBar = (activity as AppCompatActivity).supportActionBar as ActionBar
        actionBar.addOnMenuVisibilityListener { visible ->
            if (visible)
                showActionBar(false)
            else
                hideActionBarDelayed()
        }

    }

    private val handler = Handler()
    private val runnable = Runnable { this.hideActionBar() }

    private fun showActionBar(hide: Boolean) {
        actionBar.show()
        if (!hide)
            handler.removeCallbacks(runnable)
        else
            hideActionBarDelayed()
    }

    private fun hideActionBar() {
        actionBar.hide()
    }

    private fun hideActionBarDelayed() {
        val DELAY = (3 * 1000).toLong()  // 3sec
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, DELAY)
    }

    private val photoMapCameraChangeListener = GoogleMap.OnCameraChangeListener { position ->
        if (position.zoom > MAXIMUM_ZOOM || position.zoom < MINIMUM_ZOOM) {
            val zoom = (if (position.zoom > MAXIMUM_ZOOM) MAXIMUM_ZOOM else MINIMUM_ZOOM).toFloat()
            googleMap?.setOnCameraChangeListener(null)
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(position.target, zoom))
            googleMap?.animateCamera(cameraUpdate, cancelableCallback)
            return@OnCameraChangeListener
        }
        showActionBar(false)
        loaderManager.restartLoader(PHOTO_CURSOR_LOADER, Bundle(), photoListLoaderCallback)
    }

    private val cancelableCallback: GoogleMap.CancelableCallback
        get() = object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                googleMap?.setOnCameraChangeListener(photoMapCameraChangeListener)
            }

            override fun onCancel() {
                googleMap?.setOnCameraChangeListener(photoMapCameraChangeListener)
            }
        }

    private val photoListLoaderCallback = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(i: Int, bundle: Bundle): Loader<Cursor> {
            val mapBounds = googleMap!!.projection.visibleRegion.latLngBounds
            val q = QueryBuilder.createQuery(mapBounds)
            val o = QueryBuilder.sortDateNewest()
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            return CursorLoader(activity, uri, PhotoCursor.projection, q, null, o)

        }

        override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
            photoList = PhotoCursor(cursor).hashedPhotoList
            var distance = (googleMap!!.cameraPosition.zoom * 2 + 4).toInt()
            if (distance > 45) distance = 45
            subject.onNext(distance)
        }

        override fun onLoaderReset(objectLoader: Loader<Cursor>) {
        }
    }

    private var progress: Int = 0
    private var group_count: Int = 0
    private fun groupObservable(distance: Int): Observable<PhotoGroup> {
        return Observable.from(photoList).subscribeOn(Schedulers.newThread())
            .groupBy { hash -> hash.hash.binaryString.substring(0, distance) }
            .doOnNext { group_count++ }
            .concatMap {
                group -> group.map { p -> PhotoGroup(p) }
                .reduce { hashedPhotos, o -> hashedPhotos.append(o) } }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                googleMap?.clear()
                progress = 0
                group_count = 0
                listener?.showProgress(0)
                groupList?.clear()
            }
            .doOnNext { g ->
                listener?.showProgress(++progress * 10000 / group_count)
                groupList?.add(g)
                val ops = MarkerOptions().position(g.center)
                ops.icon(BitmapDescriptorFactory.defaultMarker(PhotoGroup.getMarkerColor(g.size)))
                g.marker = googleMap?.addMarker(ops)
            }
            .doOnCompleted {
                this@PhotoMapFragment.hideActionBarDelayed()
                listener?.endProgress()
            }
    }

    fun grantedPermission(granted: Boolean) {
        if (granted) {
            initMap()
        } else {
            val v = view
            if (v != null) PermissionUtils.requestPermission(v, context)
        }
    }

    companion object {
        private val PHOTO_CURSOR_LOADER = 0

        val EXTRA_GROUP = "group"
        val DEFAULT_ZOOM = 15f

        private val MAXIMUM_ZOOM = 17
        private val MINIMUM_ZOOM = 4
    }
}
