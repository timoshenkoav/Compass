package com.tunebrains.compass

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_compass.view.*


class CompassView(ctx: Context, attrs: AttributeSet) : FrameLayout(ctx, attrs) {

    init {
        View.inflate(context, R.layout.view_compass, this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val measuredWidth = measure(widthMeasureSpec)
        val measuredHeight = measure(heightMeasureSpec)

        val d = Math.min(measuredWidth, measuredHeight)

        super.onMeasure(
            MeasureSpec.makeMeasureSpec(d, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(d, MeasureSpec.EXACTLY)
        )
    }

    private fun measure(measureSpec: Int): Int {
        return MeasureSpec.getSize(measureSpec)
    }


    fun updateDegree(currentRotate: Float) {
        arrow.rotation = currentRotate % DEGREES_360
    }


    companion object {
        private const val DEGREES_360 = 360
    }
}