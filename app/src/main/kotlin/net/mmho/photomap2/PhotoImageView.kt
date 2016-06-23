package net.mmho.photomap2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

open class PhotoImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    :LoadableImageView(context,attrs,defStyle){

    private var detector: GestureDetectorCompat
    private var scaleDetector:ScaleGestureDetector
    private val TAG = "PhotoImageView"

    private val MAX_SCALE = 3f
    private val MIN_SCALE = 1f
    private var base_scale:Float = 1f

    init{
        scaleType = ScaleType.MATRIX
        detector = GestureDetectorCompat(context,object :GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, dx: Float, dy: Float): Boolean {
                val matrix = imageMatrix
                matrix.postTranslate(-dx,-dy)
                imageMatrix = matrix
                invalidate()
                return true
            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Log.d(TAG,"onDoubleTap")
                val bitmap = (drawable as BitmapDrawable).bitmap
                val matrix = imageMatrix
                val drawRect = RectF(0.0f, 0.0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                val viewRect = RectF(0.0f, 0.0f, width.toFloat(), height.toFloat())
                matrix.setRectToRect(drawRect,viewRect, Matrix.ScaleToFit.CENTER)
                imageMatrix = matrix
                invalidate()
                return true
            }

        })
        scaleDetector = ScaleGestureDetector(context,object:ScaleGestureDetector.SimpleOnScaleGestureListener(){
            private var currentScale = 1f
            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                currentScale = 1f
                return true
            }
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor / currentScale
                imageMatrix.postScale(scale,scale)
                currentScale = detector.scaleFactor

                invalidate()
                return false
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                val cur = currentScale()
                val scale = cur/base_scale
                when{
                    scale > MAX_SCALE ->{
                        val s = base_scale*MAX_SCALE/cur
                        imageMatrix.postScale(s,s)
                    }
                    scale < MIN_SCALE ->{
                        val s = base_scale*MIN_SCALE/cur
                        imageMatrix.postScale(s,s)
                    }
                }
                invalidate()
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleDetector.onTouchEvent(event)
        return detector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun setBitmap(bitmap: Bitmap){
        super.setBitmap(bitmap)
        val matrix = imageMatrix
        val drawRect = RectF(0.0f, 0.0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val viewRect = RectF(0.0f, 0.0f, width.toFloat(), height.toFloat())
        matrix.setRectToRect(drawRect,viewRect, Matrix.ScaleToFit.CENTER)
        imageMatrix = matrix
        base_scale = currentScale()
    }
    private fun currentScale():Float{
        val values = FloatArray(9)
        imageMatrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }
}