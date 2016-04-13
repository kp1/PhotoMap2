package net.mmho.photomap2

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import kotlinx.android.synthetic.main.activity_photo_list.*


class PhotoListActivity : AppCompatActivity(), ProgressChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)
        setSupportActionBar(toolbar)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment)
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                val granted = grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                (fragment as PhotoListFragment)?.grantedPermission(granted)
            }
        }
    }

    override fun showProgress(progress: Int) {
        this.progress.progress = progress
        this.progress.visibility = View.VISIBLE
    }

    override fun endProgress() {
        progress.progress = progress.max
        val fadeout = AlphaAnimation(1f, 0f)
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
        val PERMISSIONS_REQUEST = 1
    }
}
