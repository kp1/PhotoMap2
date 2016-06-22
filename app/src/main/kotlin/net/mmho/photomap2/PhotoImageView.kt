package net.mmho.photomap2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet

open class PhotoImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    :LoadableImageView(context,attrs,defStyle){

    init{
        scaleType = ScaleType.MATRIX
    }

    override fun setBitmap(bitmap: Bitmap){
        super.setBitmap(bitmap)
        val matrix = imageMatrix
        val drawRect = RectF(0.0f, 0.0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val viewRect = RectF(0.0f, 0.0f, width.toFloat(), height.toFloat())
        matrix.setRectToRect(drawRect,viewRect, Matrix.ScaleToFit.CENTER)
        imageMatrix = matrix
    }
}