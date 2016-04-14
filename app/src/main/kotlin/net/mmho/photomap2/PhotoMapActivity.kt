package net.mmho.photomap2

import android.app.Dialog
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v4.view.WindowCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import kotlinx.android.synthetic.main.activity_photo_map.*
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.IOException

class PhotoMapActivity : AppCompatActivity(), ProgressChangeListener {

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val searchQuery = intent.getStringExtra(SearchManager.QUERY)
            Observable
                .create { subscriber: Subscriber<in List<Address>> ->
                    var data: List<Address>? = null
                    try {
                        data = Geocoder(applicationContext).getFromLocationName(searchQuery, 5)
                    } catch (e: IOException) {
                        subscriber.onError(e)
                    }

                    subscriber.onNext(data)
                    subscriber.onCompleted()
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(next@ { list ->
                    if (list == null || list.size == 0) {
                        Toast.makeText(this@PhotoMapActivity.applicationContext,
                            this@PhotoMapActivity.getString(R.string.location_not_found, searchQuery),
                            Toast.LENGTH_LONG).show()
                        return@next
                    }

                    val suggestions = SearchRecentSuggestions(this@PhotoMapActivity,
                        MapSuggestionProvider.Companion.AUTHORITY,
                        MapSuggestionProvider.Companion.MODE)
                    suggestions.saveRecentQuery(searchQuery, null)

                    if (list.size == 1) {
                        val fragment = this@PhotoMapActivity.supportFragmentManager.findFragmentById(R.id.map)
                        val update = CameraUpdateFactory.newLatLngZoom(AddressUtil.addressToLatLng(list[0]), PhotoMapFragment.Companion.DEFAULT_ZOOM)
                        if (fragment is PhotoMapFragment)
                            fragment.getMapAsync { map -> map.moveCamera(update) }
                    } else {
                        val transaction = this@PhotoMapActivity.supportFragmentManager.beginTransaction()
                        val prev = this@PhotoMapActivity.supportFragmentManager.findFragmentByTag(TAG_DIALOG)
                        if (prev != null) transaction.remove(prev)
                        transaction.addToBackStack(null)
                        transaction.commit()

                        val fragment = SearchResultDialogFragment.Companion.newInstance(searchQuery, list)
                        fragment.show(this@PhotoMapActivity.supportFragmentManager, TAG_DIALOG)
                    }
                })
                .doOnError {
                    Toast.makeText(this@PhotoMapActivity.applicationContext, this@PhotoMapActivity.getString(R.string.location_not_found, searchQuery),
                        Toast.LENGTH_LONG).show()
                }
                .subscribe()
        }
    }

    override fun onResume() {
        super.onResume()
        val api = GoogleApiAvailability.getInstance()
        val result = api.isGooglePlayServicesAvailable(this)
        if (result == ConnectionResult.SUCCESS) {
            val fragment = supportFragmentManager.findFragmentById(R.id.map)
            if (fragment !is PhotoMapFragment) {
                val mapFragment = PhotoMapFragment()
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.map, mapFragment).commit()
            }
        } else if (api.isUserResolvableError(result)) {
            api.showErrorDialogFragment(this, result, 1) { dialog -> finish() }
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        val fragment = supportFragmentManager.findFragmentById(R.id.map)
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                val granted = grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (fragment != null && fragment is PhotoMapFragment) {
                    fragment.grantedPermission(granted)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        }
        setContentView(R.layout.activity_photo_map)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar?)
        val bar = supportActionBar
        bar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun showProgress(progress: Int) {
        this.progress.progress = progress
        this.progress.visibility = View.VISIBLE
    }

    override fun endProgress() {
        progress.progress = progress.max
        val fadeout: AlphaAnimation
        fadeout = AlphaAnimation(1f, 0f)
        fadeout.duration = 300
        fadeout.fillAfter = true
        fadeout.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                progress.visibility = View.GONE
                progress.animation = null
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        progress.startAnimation(fadeout)
    }

    companion object {

        private val TAG_DIALOG = "dialog"
        val PERMISSIONS_REQUEST = 1
    }

}
