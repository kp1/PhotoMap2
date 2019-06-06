package net.mmho.photomap2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import androidx.core.view.MenuItemCompat
import androidx.viewpager.widget.ViewPager
import androidx.core.view.WindowCompat
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.fragment_photo_view.*

class PhotoViewActivity : AppCompatActivity() {

    private var adapter: PhotoViewAdapter? = null
    private var shareActionProvider: ShareActionProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras

        if (bundle == null) {
            finish()
            return
        }

        supportRequestWindowFeature(WindowCompat.FEATURE_ACTION_BAR_OVERLAY)
        setContentView(R.layout.fragment_photo_view)
        setSupportActionBar(toolbar)
        showActionBar(true)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.addOnMenuVisibilityListener(menuVisibilityListener)

        val group = bundle.getParcelable<PhotoGroup>(EXTRA_GROUP)
        title = group?.title

        val position = bundle.getInt(EXTRA_POSITION)
        adapter = PhotoViewAdapter(supportFragmentManager, group)

        photo_pager.adapter = adapter
        photo_pager.addOnPageChangeListener(onPageChangeListener)
        photo_pager.currentItem = position
        photo_pager.pageMargin = 30
        photo_pager.setOnClickListener(onClickListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        photo_pager.removeOnPageChangeListener(onPageChangeListener)
    }


    private val handler = Handler()
    private val runnable = Runnable { this.hideActionBar() }

    private fun showActionBar(hide: Boolean) {
        supportActionBar?.show()
        if (!hide)
            handler.removeCallbacks(runnable)
        else
            hideActionBarDelayed()
    }

    private fun hideActionBar() {
        supportActionBar?.hide()
    }

    private fun hideActionBarDelayed() {
        val delay = (3 * 1000).toLong()
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, delay)
    }


    private val menuVisibilityListener = ActionBar.OnMenuVisibilityListener {
        when {
            it -> showActionBar(false)
            else -> hideActionBarDelayed()
        }
    }

    private val onClickListener = View.OnClickListener {
        val bar = supportActionBar
        when {
            bar == null -> {}
            bar.isShowing -> hideActionBar()
            else -> showActionBar(true)
        }
    }

    private val onPageChangeListener = object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(i: Int) {
            shareActionProvider?.setShareIntent(setShareIntent(Intent(), i))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.photo_view_menu, menu)
        val share = menu.findItem(R.id.share)
        shareActionProvider = MenuItemCompat.getActionProvider(share) as ShareActionProvider
        shareActionProvider?.setShareIntent(setShareIntent(Intent(), photo_pager.currentItem))
        shareActionProvider?.setVisibilityListener { visible ->
            if (visible)
                showActionBar(false)
            else
                hideActionBarDelayed()
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun setShareIntent(intent: Intent, index: Int): Intent {
        val imageId = adapter?.getItemID(index)
        val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageId.toString())
        intent.action = Intent.ACTION_SEND
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "image/jpeg"
        return intent
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.map -> {
                intent = Intent(this, PhotoMapActivity::class.java)
                setShareIntent(intent, photo_pager.currentItem)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val i = Intent()
        val b = Bundle()
        b.putInt(EXTRA_POSITION, photo_pager.currentItem)
        i.putExtras(b)
        setResult(Activity.RESULT_OK, i)
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_GROUP = "photo_group"
        const val EXTRA_POSITION = "position"
    }
}
