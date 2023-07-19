package com.udacity

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates


class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val TAG = "LoadingButton"
    private var widthSize = 0
    private var heightSize = 0
    private val valueAnimator = ValueAnimator.ofInt(0, 360).setDuration(1000)
    @SuppressLint("ObjectAnimatorBinding")
    private var btnBackgroundColor = 0
    private var btnTextColor = 0
    private var btnLoadingColor = 0
    private var btnCircleColor = 0

    private var btnTextStr = ""
    private var progress = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        typeface = Typeface.create( "", Typeface.BOLD)
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                btnTextStr = resources.getString(R.string.button_loading)
                valueAnimator.start()
            }
            ButtonState.Completed -> {
                btnTextStr = resources.getString(R.string.button_name)
                valueAnimator.cancel()
                progress = 0
            }
            else -> {
                Log.d(TAG, "button clicked")
            }
        }
        invalidate()
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            btnBackgroundColor = getColor(R.styleable.LoadingButton_backgroundColor, 0)
            btnTextColor = getColor(R.styleable.LoadingButton_textColor, 0)
            btnLoadingColor = getColor(R.styleable.LoadingButton_buttonLoadingColor, 0)
            btnCircleColor = getColor(R.styleable.LoadingButton_buttonCircleColor, 0)
        }
        buttonState = ButtonState.Completed
        valueAnimator.apply {
            addUpdateListener {
                progress = it.animatedValue as Int
                invalidate()
            }
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = btnBackgroundColor
        canvas?.drawRect(0f,0f,widthSize.toFloat(), heightSize.toFloat(), paint)

        paint.color = btnLoadingColor
        canvas?.drawRect(0f, 0f, widthSize * progress / 360f, heightSize.toFloat(), paint)

        paint.color = btnTextColor
        canvas?.drawText(btnTextStr, widthSize / 2.0f, heightSize / 3.0f + 30.0f, paint)

        paint.color = btnCircleColor
        canvas?.drawArc(widthSize - 150f,50f,widthSize - 50f,150f,0f, progress.toFloat(), true, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}