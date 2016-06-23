package net.mmho.photomap2

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlinx.android.synthetic.main.fragment_photo_view.*

open class PhotoImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    :LoadableImageView(context,attrs,defStyle){

    private var detector: GestureDetectorCompat
    private var scaleDetector:ScaleGestureDetector
    private val TAG = "PhotoImageView"

    private val MAX_SCALE = 3f
    private val MIN_SCALE = 1f
    private lateinit var baseMatrix:Matrix

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
                imageMatrix = baseMatrix
                invalidate()
                return true
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                (context as Activity).photo_pager?.performClick()
                return true
            }

        })
        scaleDetector = ScaleGestureDetector(context,object:ScaleGestureDetector.SimpleOnScaleGestureListener(){
            private var currentScale = 1f
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
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
                val base_scale = baseMatrix.scale()
                val scale = cur/base_scale
                val s =
                    when{
                        scale > MAX_SCALE -> base_scale*MAX_SCALE/cur
                        scale < MIN_SCALE -> base_scale*MIN_SCALE/cur
                        else -> 1f
                    }
                imageMatrix.postScale(s,s)
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
        baseMatrix = Matrix()
        val drawRect = RectF(0.0f, 0.0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val viewRect = RectF(0.0f, 0.0f, width.toFloat(), height.toFloat())
        baseMatrix.setRectToRect(drawRect, viewRect, Matrix.ScaleToFit.CENTER)
        imageMatrix = baseMatrix
    }

    private fun currentScale():Float{
        return imageMatrix.scale()
    }
}

fun Matrix.scale():Float{
    val values = FloatArray(9)
    this.getValues(values)
    return values[Matrix.MSCALE_X]
}