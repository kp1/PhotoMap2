package net.mmho.photomap2

import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v4.view.WindowCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_photo_map.*
import java.io.IOException

class PhotoMapActivity : AppCompatActivity(), ProgressChangeListener {

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val searchQuery = intent.getStringExtra(SearchManager.QUERY)
            Single
                .create { subscriber:SingleEmitter<List<Address>> ->
                    val data: List<Address> =
                    try {
                        Geocoder(applicationContext).getFromLocationName(searchQuery, 5)
                    } catch (e: IOException) {
                        listOf()
                    }
                    subscriber.onSuccess(data)
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess { list ->
                    when(list.size){
                        0 ->{
                            Toast.makeText(applicationContext,
                                getString(R.string.location_not_found, searchQuery),
                                Toast.LENGTH_LONG).show()
                        }
                        1 ->{
                            SearchRecentSuggestions(this,
                                MapSuggestionProvider.AUTHORITY,
                                MapSuggestionProvider.MODE).run{
                                saveRecentQuery(searchQuery, null)
                            }
                            val fragment = supportFragmentManager.findFragmentById(R.id.map)
                            val update = CameraUpdateFactory.newLatLngZoom(list[0].toLatLng(), PhotoMapFragment.DEFAULT_ZOOM)
                            (fragment as? PhotoMapFragment)?.getMapAsync { map -> map.moveCamera(update) }
                        }
                        else ->{
                            SearchRecentSuggestions(this,
                                MapSuggestionProvider.AUTHORITY,
                                MapSuggestionProvider.MODE).run{
                                saveRecentQuery(searchQuery, null)
                            }

                            supportFragmentManager.beginTransaction().apply{
                                val prev = supportFragmentManager.findFragmentByTag(TAG_DIALOG)
                                if (prev != null) remove(prev)
                                addToBackStack(null)
                            }.commit()
                            val fragment = SearchResultDialogFragment.newInstance(searchQuery, list)
                            fragment.show(supportFragmentManager, TAG_DIALOG)
                        }
                    }
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
            api.showErrorDialogFragment(this, result, 1) { finish() }
        } else {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        val fragment = supportFragmentManager.findFragmentById(R.id.map)
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (fragment != null && fragment is PhotoMapFragment) {
                    fragment.grantedPermission(granted)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        }
        setContentView(R.layout.activity_photo_map)
        setSupportActionBar(toolbar)
        val bar = supportActionBar
        bar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun showProgress(progress: Int) {
        this.progress.progress = progress
        this.progress.visibility = View.VISIBLE
    }

    override fun endProgress() {
        progress.progress = progress.max
        val fadeout = AlphaAnimation(1f, 0f).apply{
            duration = 300
            fillAfter = true
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    progress.visibility = View.GONE
                    progress.animation = null
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
        progress.startAnimation(fadeout)
    }

    companion object {
        private const val TAG_DIALOG = "dialog"
        const val PERMISSIONS_REQUEST = 1
    }

}
