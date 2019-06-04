package com.tunebrains.compass

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_compass.view.*


class CompassView(ctx: Context, attrs: AttributeSet) : FrameLayout(ctx, attrs) {
    private val CENTER = 0.5f
    private val FAST_ANIMATION_DURATION = 200L
    private val DEGREES_360 = 360
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

    private var lastRotation = 0f

    fun updateDegree(currentRotate: Float) {
        val animToDegree = getShortestPathEndPoint(lastRotation, currentRotate)

        val rotateAnimation = RotateAnimation(
            lastRotation, animToDegree,
            Animation.RELATIVE_TO_SELF, CENTER, Animation.RELATIVE_TO_SELF, CENTER
        )
        rotateAnimation.duration = FAST_ANIMATION_DURATION
        rotateAnimation.fillAfter = true
//        rotateAnimation.setAnimationListener(this)

        lastRotation = currentRotate

        arrow.startAnimation(rotateAnimation)
//        arrow.animate().rotation(it)
    }

    private fun getShortestPathEndPoint(start: Float, end: Float): Float {
        var end = end
        val delta = deltaRotation(start, end)
        val invertedDelta = invertedDelta(start, end)
        if (Math.abs(invertedDelta) < Math.abs(delta)) end = start + invertedDelta
        return end
    }

    private fun deltaRotation(start: Float, end: Float): Float {
        return end - start
    }

    private fun invertedDelta(start: Float, end: Float): Float {
        var end = end
        val delta = end - start
        if (delta < 0)
            end += DEGREES_360
        else
            end += -DEGREES_360
        return end - start
    }
}