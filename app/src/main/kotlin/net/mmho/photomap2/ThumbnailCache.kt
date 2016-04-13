package net.mmho.photomap2

import android.graphics.Bitmap
import android.support.v4.util.LruCache

class ThumbnailCache private constructor(maxSize: Int) : LruCache<Long, Bitmap>(maxSize) {

    override fun sizeOf(key: Long?, bitmap: Bitmap): Int {
        return bitmap.rowBytes * bitmap.height / 1024
    }

    companion object {

        private var self: ThumbnailCache? = null
        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private val cacheSize = maxMemory / 8

        val instance: ThumbnailCache
            get() {
                return self ?: ThumbnailCache(cacheSize)
            }
    }
}
