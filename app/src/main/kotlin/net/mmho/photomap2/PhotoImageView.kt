package net.mmho.photomap2

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.util.Log
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
    private lateinit var bitmap:Bitmap

    init{
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context,object:ScaleGestureDetector.SimpleOnScaleGestureListener(){
            private var currentScale = 1f
            private var focusX = 0f
            private var focusY = 0f
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                currentScale = 1f
                focusX = detector.focusX
                focusY = detector.focusY
                return true
            }
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = detector.scaleFactor / currentScale
                imageMatrix.postScale(scale,scale,focusX,focusY)
                currentScale = detector.scaleFactor

                invalidate()
                return false
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                val base_scale = baseMatrix.scale()
                val values = FloatArray(9)
                imageMatrix.getValues(values)
                var cur = values[Matrix.MSCALE_X]

                val scale = cur/base_scale
                val s =
                    when{
                        scale > MAX_SCALE -> base_scale*MAX_SCALE/cur
                        scale < MIN_SCALE -> base_scale*MIN_SCALE/cur
                        else -> 1f
                    }
                cur *= s
                imageMatrix.postScale(s,s)

                imageMatrix.getValues(values)
                val tx = values[Matrix.MTRANS_X]
                val ty = values[Matrix.MTRANS_Y]

                val x = when {
                    bitmap.width*cur < width -> (width - bitmap.width*cur)/2-tx
                    tx>0 -> -tx
                    bitmap.width*cur +tx < width -> width-bitmap.width*cur-tx
                    else -> 0f
                }
                val y = when {
                    bitmap.height*cur < height -> (height-bitmap.height*cur)/2-ty
                    ty>0 -> -ty
                    bitmap.height*cur + ty < height -> height-bitmap.height*cur-ty
                    else -> 0f
                }
                imageMatrix.postTranslate(x,y)

                invalidate()
            }
        })
        detector = GestureDetectorCompat(context,object :GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, dx: Float, dy: Float): Boolean {
                if(scaleDetector.isInProgress) return false

                val matrix = imageMatrix
                Log.d(TAG,"onScroll DX:$dx,DY:$dy")
                val values = FloatArray(9)
                matrix.getValues(values)
                val tx = values[Matrix.MTRANS_X]
                val ty = values[Matrix.MTRANS_Y]
                val scale = values[Matrix.MSCALE_X]

                val x = when{
                    // 左
                    dx > 0 -> {
                        val right = tx + bitmap.width * scale - width
                        if (right > 0f) Math.min(dx, right) else 0f
                    }
                    // 右
                    else -> if(tx < 0f) Math.max(dx,tx) else 0f
                }

                val y = when{
                    // 上
                    dy > 0 -> {
                        val bottom = ty+bitmap.height*scale-height
                        if(bottom > 0f) Math.min(dy,bottom) else 0f
                    }
                    // 下
                    else -> if(ty < 0f) Math.max(dy,ty) else 0f

                }

                matrix.postTranslate(-x,-y)

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
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleDetector.onTouchEvent(event)
        return detector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun setBitmap(bmp: Bitmap){
        bitmap = bmp
        super.setBitmap(bitmap)
        baseMatrix = Matrix()
        val drawRect = RectF(0.0f, 0.0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val viewRect = RectF(0.0f, 0.0f, width.toFloat(), height.toFloat())
        baseMatrix.setRectToRect(drawRect, viewRect, Matrix.ScaleToFit.CENTER)
        imageMatrix = baseMatrix
    }

}

fun Matrix.scale():Float{
    val values = FloatArray(9)
    this.getValues(values)
    return values[Matrix.MSCALE_X]
}