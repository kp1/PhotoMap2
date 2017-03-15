package net.mmho.photomap2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.provider.MediaStore.Images.Media.*
import android.provider.MediaStore.Images.Thumbnails.MINI_KIND
import android.provider.MediaStore.Images.Thumbnails.getThumbnail
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
    private var id: Long = -1L

    private var subject: PublishSubject<Long> = PublishSubject.create<Long>()
    private var subscription: Subscription? = null

    init {
        subscription = subject
            .switchMap { id ->
                loadImageObservable(id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            .subscribe { setBitmap(it) }
    }

    open fun setBitmap(bitmap: Bitmap) {
        setImageBitmap(bitmap)
    }

    fun load(image_id: Long) {

        when (id) {
            image_id -> return
            else -> id = image_id
        }

        val bitmap: Bitmap? = if(thumbnail) ThumbnailCache.instance.get(image_id) else null
        setImageBitmap(bitmap)

        if (bitmap != null) return

        subject.onNext(image_id)
    }

    private fun loadImageObservable(image_id: Long): Observable<Bitmap> {
        return Observable.create { subscriber ->
            val c = query(context.contentResolver, EXTERNAL_CONTENT_URI,arrayOf(_ID,ORIENTATION,DATA),
                QueryBuilder.createQuery(image_id), null, null)

            var bmp: Bitmap? = null

            if (thumbnail) {
                bmp = getThumbnail(context.contentResolver, image_id, MINI_KIND, null)
            }

            if (c.count > 0) {
                c.moveToFirst()
                val orientation = c.getInt(c.getColumnIndexOrThrow(ORIENTATION))

                if (!thumbnail) {
                    val option = BitmapFactory.Options()
                    val path = c.getString(c.getColumnIndexOrThrow(DATA))

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
            if(bmp!=null) subscriber.onNext(bmp)
            subscriber.onCompleted()
            c.close()
        }
    }
}
