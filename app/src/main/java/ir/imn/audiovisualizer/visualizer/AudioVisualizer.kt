package ir.imn.audiovisualizer.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.RectF
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.View
import kotlin.math.*


class AudioVisualizer(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val barsCount = 16
    private val barsColor = Color.argb(200, 181, 111, 233)
    private val backgroundColor = Color.parseColor("#EEEEEE")

    private val piePaint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            isAntiAlias = true
            color = barsColor
        }

    init {
        setBackgroundColor(backgroundColor)
    }

    private var magnitudes = floatArrayOf()
    private val data = mutableListOf<RectF>()

    private var visualizer: Visualizer? = null

    private val dataCaptureListener = object : Visualizer.OnDataCaptureListener {

        override fun onFftDataCapture(v: Visualizer?, data: ByteArray?, sampleRate: Int) {
            data?.let {
                magnitudes = convertFFTtoMagnitudes(data)
                visualizeData()
            }
        }

        override fun onWaveFormDataCapture(v: Visualizer?, data: ByteArray?, sampleRate: Int) {
//            data?.let {
//                bytes = data
//                visualizeData()
//            }
        }

    }

    fun link(mediaPlayer: MediaPlayer) {
        if (visualizer != null) return
        visualizer = Visualizer(mediaPlayer.audioSessionId)
            .apply {
                captureSize = Visualizer.getCaptureSizeRange()[0]
                setDataCaptureListener(
                    dataCaptureListener,
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    true
                )
                enabled = true
            }

        mediaPlayer.setOnCompletionListener { visualizer?.enabled = false }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        visualizeData()
    }

    var maxAmp = 0f

    fun visualizeData() {
        data.clear()


        val barWidth = width / (barsCount * 2f)
        for (i in 0 until barsCount) {
            val segmentSize = (magnitudes.size / barsCount.toFloat())
            val segmentStart = i * segmentSize
            val segmentEnd = segmentStart + segmentSize

            var sum = 0f
            for (j in segmentStart.toInt() until segmentEnd.toInt()) {
                sum += magnitudes[j]
            }
            val amp = sum
                .run { this / segmentSize } // average
//                .run { this / 8 }
                .run { this * height }
                .run { max(this, barWidth) }


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
        invalidate()
    }

    private fun printData() {
        val temp = mutableListOf<Float>()
        data.forEach { temp.add(it.top) }

        println("imns ${temp.size} ${temp.min()} ${temp.max()} $temp ")
    }

    private var maxMagnitude = 0f

    private fun convertFFTtoMagnitudes(fft: ByteArray): FloatArray {
        if (fft.isEmpty()) {
            return floatArrayOf()
        }

        val n: Int = fft.size
        val magnitudes = FloatArray(n / 2)

        for (k in 0 until n / 2 - 1) {
            val i = (k + 1) * 2
            magnitudes[k] = hypot(fft[i].toDouble(), fft[i + 1].toDouble()).toFloat()
        }

        val localMax = magnitudes.max() ?: 0f
        if (localMax > maxMagnitude) {
            maxMagnitude = localMax
            println("imns $maxMagnitude")
        }


        return magnitudes.map { it / maxMagnitude }.toFloatArray()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            data.forEach {
                drawRoundRect(it, 25f, 25f, piePaint)
            }
        }
    }

    override fun onDetachedFromWindow() {
        visualizer?.release()
        super.onDetachedFromWindow()
    }
}