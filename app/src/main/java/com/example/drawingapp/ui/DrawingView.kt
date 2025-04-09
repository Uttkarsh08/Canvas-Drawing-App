package com.example.drawingapp.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.drawingapp.model.DrawingPath

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var path = Path()
    private var paint = Paint()
    private var paths = mutableListOf<DrawingPath>()

    init {
        setupPaint()
    }

    private fun setupPaint() {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 10f
        paint.isAntiAlias = true
    }


    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
    }

    fun updatePaths(newPaths: List<DrawingPath>) {
        paths = newPaths.toMutableList()
        invalidate()
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paths.forEach { drawingPath ->
            canvas.drawPath(drawingPath.path, drawingPath.paint)
        }
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path = Path()
                path.moveTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                paths.add(DrawingPath(path, Paint(paint)))
                (context as? MainActivity)?.onPathDrawn(DrawingPath(path, Paint(paint)))
                path = Path()
                invalidate()
            }
        }
        return true
    }

    fun setColor(newColor: Int) {
        paint.color = newColor
        Log.d("DrawingView", "Pen color changed to: $newColor")
        invalidate()
    }
}
