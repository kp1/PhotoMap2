package net.mmho.photomap2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

open class PhotoImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    :LoadableImageView(context,attrs,defStyle){

    private var detector: GestureDetectorCompat
    private val TAG = "PhotoImageView"

    init{
        scaleType = ScaleType.MATRIX
        detector = GestureDetectorCompat(context,object :GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent?): Boolean {
                Log.d(TAG,"onDown")
                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Log.d(TAG,"onDoubleTap")
                return false
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                Log.d(TAG,"dx:$distanceX,dy:$distanceY")
                val matrix = imageMatrix
                matrix.postTranslate(-distanceX,-distanceY)
                imageMatrix = matrix
                invalidate()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        return detector.onTouchEvent(event) || super.onTouchEvent(event)
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