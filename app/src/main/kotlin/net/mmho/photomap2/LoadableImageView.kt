package net.mmho.photomap2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.provider.MediaStore
import android.provider.MediaStore.Images.ImageColumns.*
import android.util.AttributeSet
import android.widget.ImageView
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject

open class LoadableImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
        : ImageView(context, attrs, defStyle) {

    internal var thumbnail = false
    private var w: Int = 0
    private var id: Long = -1L

    private var subject: PublishSubject<Long>
    private var subscription: Subscription? = null

    init {
        subject = PublishSubject.create<Long>()
        subscription = subject.onBackpressureLatest().
            subscribeOn(Schedulers.newThread()).
            switchMap { id -> this@LoadableImageView.loadImage(id).subscribeOn(Schedulers.newThread()) }.
            observeOn(AndroidSchedulers.mainThread()).
            subscribe { bmp -> setImageBitmap(bmp) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        subscription?.unsubscribe()
        subscription = null
    }

    fun startLoading(image_id: Long) {

        when (id) {
            image_id -> return
            else -> id = image_id
        }

        var bitmap: Bitmap? = if(thumbnail) ThumbnailCache.instance.get(image_id) else null
        setImageBitmap(bitmap)

        if (bitmap != null) return

        post {
            w = Math.min(this@LoadableImageView.width, this@LoadableImageView.height)
            subject.onNext(image_id)
        }
    }

    private fun loadImage(image_id: Long): Observable<Bitmap> {
        return Observable.create { subscriber ->
            val projection = arrayOf(_ID,ORIENTATION,DATA)
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val c = MediaStore.Images.Media.query(this@LoadableImageView.context.contentResolver, uri, projection,
                QueryBuilder.createQuery(image_id), null, null)

            var bmp: Bitmap? = null

            if (thumbnail) {
                bmp = MediaStore.Images.Thumbnails.getThumbnail(this@LoadableImageView.context.contentResolver, image_id,
                    MediaStore.Images.Thumbnails.MINI_KIND, null)
            }

            if (c.count > 0) {
                c.moveToFirst()
                val orientation = c.getInt(c.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION))

                if (!thumbnail) {
                    val option = BitmapFactory.Options()
                    val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))

                    // get only size
                    option.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(path, option)


                    val s = Math.max(option.outHeight, option.outWidth) / w + 1
                    var scale = 1
                    while (scale < s) scale *= 2

                    option.inSampleSize = scale
                    option.inJustDecodeBounds = false
                    option.inPreferredConfig = Bitmap.Config.RGB_565
                    bmp = BitmapFactory.decodeFile(path, option)
                }

                if (bmp != null && orientation != 0) {
                    val matrix = Matrix()
                    matrix.setRotate(orientation.toFloat())
                    val oldBmp = bmp
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, false)
                    oldBmp.recycle()
                }
            }
            if (thumbnail && bmp != null) ThumbnailCache.instance.put(image_id, bmp)
            subscriber.onNext(bmp)
            subscriber.onCompleted()
            c.close()
        }
    }
}
