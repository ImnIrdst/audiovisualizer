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
import kotlin.math.log10
import kotlin.math.max


class AudioVisualizer(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val smoothingFactor = 0.2f
    private val barsCount = 24
    private val barsColor = Color.argb(200, 181, 111, 233)
    private val backgroundColor = Color.parseColor("#EEEEEE")

    private val piePaint = Paint(ANTI_ALIAS_FLAG)
        .apply {
            isAntiAlias = true
            color = barsColor
        }

    init {
        setBackgroundColor(backgroundColor) // showing bounding box of the view
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

        override fun onWaveFormDataCapture(v: Visualizer?, data: ByteArray?, sampleRate: Int) = Unit
    }

    fun link(mediaPlayer: MediaPlayer) {
        if (visualizer != null) return
        visualizer = Visualizer(mediaPlayer.audioSessionId)
            .apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(
                    dataCaptureListener,
                    Visualizer.getMaxCaptureRate() * 2 / 3,
                    false,
                    true
                )
                enabled = true
            }

        mediaPlayer.setOnCompletionListener { visualizer?.enabled = false }
    }

    fun visualizeData() {
        data.clear()

        val barWidth = width / (barsCount * 2f) // 2f is for the space between bars
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
                .run { this * height } // normalize to the height of the view
                .run { max(this, barWidth) } // at least shows a circle

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

    private val maxMagnitude = calculateMagnitude(128f, 128f)

    private fun convertFFTtoMagnitudes(fft: ByteArray): FloatArray {
        if (fft.isEmpty()) {
            return floatArrayOf()
        }

        val n: Int = fft.size / FFT_NEEDED_PORTION
        val curMagnitudes = FloatArray(n / 2)

        var prevMagnitudes = magnitudes
        if (prevMagnitudes.isEmpty()) {
            prevMagnitudes = FloatArray(n)
        }

        for (k in 0 until n / 2 - 1) {
            val index = k * FFT_STEP + FFT_OFFSET
            val real: Byte = fft[index]
            val imaginary: Byte = fft[index + 1]

            val curMagnitude = calculateMagnitude(real.toFloat(), imaginary.toFloat())
            curMagnitudes[k] = curMagnitude + (prevMagnitudes[k] - curMagnitude) * smoothingFactor
        }
        return curMagnitudes.map { it / maxMagnitude }.toFloatArray()
    }

    private fun calculateMagnitude(r: Float, i: Float) =
        if (i == 0f && r == 0f) 0f else 10 * log10(r * r + i * i)


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        visualizeData()
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

    companion object {
        private const val FFT_STEP = 2
        private const val FFT_OFFSET = 2
        private const val FFT_NEEDED_PORTION = 3 // 1/3
    }
}