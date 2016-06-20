package net.mmho.photomap2

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.photo_view.view.*

class PhotoViewFragment : Fragment() {
    private var image_id: Long = 0
    private var image: LoadableImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        image_id = arguments.getLong(EXTRA_IMAGE_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.photo_view, container, false)
        image = v.photo
        image?.startLoading(image_id)
        return v
    }

    override fun onDestroyView() {
        super.onDestroyView()
        image?.setImageDrawable(null)
    }

    companion object {

        val EXTRA_IMAGE_ID = "image_id"
    }
}
