package net.mmho.photomap2

import android.graphics.Bitmap
import android.support.v4.util.LruCache

class ThumbnailCache private constructor(maxSize: Int) : LruCache<Long, Bitmap>(maxSize) {

    override fun sizeOf(key: Long?, bitmap: Bitmap): Int {
        return bitmap.rowBytes * bitmap.height / 1024
    }

    private object Self { val INSTANCE = ThumbnailCache(cacheSize) }

    companion object {
        val instance:ThumbnailCache by lazy { Self.INSTANCE }

        private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        private val cacheSize = maxMemory / 8

    }
}
