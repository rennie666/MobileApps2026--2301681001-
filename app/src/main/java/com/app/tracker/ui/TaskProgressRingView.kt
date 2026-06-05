package com.app.tracker.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class TaskProgressRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private var animatedProgress: Float = 0f
    private var maxProgress: Float = 100f
    private var strokeWidth: Float = 24f

    // Colors
    private val backgroundRingColor = Color.parseColor("#E5E9F0")
    private val activeRingColor = Color.parseColor("#121212") // dark_accent
    private val textColor = Color.parseColor("#121212")

    // Paints
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundRingColor
        style = Paint.Style.STROKE
        this.strokeWidth = this@TaskProgressRingView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = activeRingColor
        style = Paint.Style.STROKE
        this.strokeWidth = this@TaskProgressRingView.strokeWidth
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        textAlign = Paint.Align.CENTER
        textSize = 64f
        isFakeBoldText = true
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#707070")
        textAlign = Paint.Align.CENTER
        textSize = 28f
    }

    private val rectF = RectF()

    fun setProgress(progressValue: Float) {
        val newProgress = progressValue.coerceIn(0f, maxProgress)
        val animator = ValueAnimator.ofFloat(animatedProgress, newProgress)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            animatedProgress = animation.animatedValue as Float
            invalidate()
        }
        animator.start()
        this.progress = newProgress
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = strokeWidth / 2 + 10f
        rectF.set(
            padding,
            padding,
            w.toFloat() - padding,
            h.toFloat() - padding
        )
        // Auto scale text size based on dimensions
        val minDim = Math.min(w, h)
        textPaint.textSize = minDim * 0.22f
        labelPaint.textSize = minDim * 0.08f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // Draw background ring
        canvas.drawOval(rectF, backgroundPaint)

        // Draw active progress arc (starting from -90 degrees, i.e., 12 o'clock)
        val sweepAngle = (animatedProgress / maxProgress) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, activePaint)

        // Draw percentage text
        val percentText = "${animatedProgress.toInt()}%"
        val textHeight = textPaint.descent() - textPaint.ascent()
        val textOffset = textHeight / 2 - textPaint.descent()
        canvas.drawText(percentText, cx, cy + textOffset - 8f, textPaint)

        // Draw helper label below percent
        canvas.drawText("Done", cx, cy + textOffset + (textPaint.textSize * 0.45f), labelPaint)
    }
}
