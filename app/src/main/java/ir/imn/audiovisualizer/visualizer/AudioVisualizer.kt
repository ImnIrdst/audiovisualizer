package ir.imn.audiovisualizer.visualizer

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class AudioVisualizer(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val bars = 16

    private val piePaint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            isAntiAlias = true
            color = Color.argb(200, 181, 111, 233)
        }

    init {
        setBackgroundColor(Color.parseColor("#EEEEEE"))
    }

    private var data = mutableListOf<RectF>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val barWidth = width / (bars * 2f)
        for (i in 0..bars) {
            data.add(
                RectF(
                    barWidth * i * 2 + barWidth / 2,
                    0f,
                    barWidth * (i * 2 + 1) + barWidth / 2,
                    Random.nextFloat() * height
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            data.forEach {
                drawRoundRect(it, 25f, 25f, piePaint)
            }
        }
    }

}