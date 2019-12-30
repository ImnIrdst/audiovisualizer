package ir.imn.audiovisualizer.visualizer

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
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
            val amp = max(Random.nextFloat() * height, barWidth)

            val horizontalOffset = barWidth / 2 // remedy for last empty bar

            val startX = barWidth * i * 2
            val endX = startX + barWidth

            val midY = height / 2
            val startY = midY - amp / 2
            val endY = midY + amp / 2

            data.add(
                RectF(startX + horizontalOffset, startY, endX + horizontalOffset, endY)
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