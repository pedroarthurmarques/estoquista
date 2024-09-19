package br.com.pedroamarques.estoquista.views

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import br.com.pedroamarques.estoquista.R

class WaveFormView: View {

    constructor(context: Context?): super(context) {
        setUp()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?
    ): super(context, attrs) {
        setUp()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ): super(context, attrs, defStyleAttr) {
        setUp()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ): super(context, attrs, defStyleAttr, defStyleRes) {
        setUp()
    }

    private val defaultFrequency = 1.5f
    private val defaultAmplitude = 1.0f
    private val defaultIdleAmplitude = 0.01f
    private val defaultNumberOfWaves = 5.0f
    private val defaultPhaseShift = -0.15f
    private val defaultDensity = 5.0f
    private val defaultPrimaryLineWidth = 3.0f
    private val defaultSecondaryLineWidth = 1.0f

    private var phase = 0f
    private var amplitude = 0f
    private var frequency = 0f
    private var idleAmplitude = 0f
    private var numberOfWaves = 0f
    private var phaseShift = 0f
    private var density = 0f
    private var primaryWaveLineWidth = 0f
    private var secondaryWaveLineWidth = 0f
    var mPaintColor: Paint? = null
    var rect: Rect? = null

    var isStraightLine = false

    private fun setUp() {
        frequency = defaultFrequency
        amplitude = defaultAmplitude
        idleAmplitude = defaultIdleAmplitude
        numberOfWaves = defaultNumberOfWaves
        phaseShift = defaultPhaseShift
        density = defaultDensity
        primaryWaveLineWidth = defaultPrimaryLineWidth
        secondaryWaveLineWidth = defaultSecondaryLineWidth
        mPaintColor = Paint()
        mPaintColor!!.color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    fun updateAmplitude(ampli: Float, isSpeaking: Boolean) {
        amplitude = Math.max(ampli, idleAmplitude)
        isStraightLine = isSpeaking
    }


    override fun onDraw(canvas: Canvas) {
        rect = Rect(0, 0, canvas.width, canvas.width)
        canvas.drawColor(Color.TRANSPARENT)
        /*canvas.drawRect(rect, mPaintColor);*/if (isStraightLine) {
            var i = 0
            while (i < numberOfWaves) {
                mPaintColor!!.strokeWidth =
                    if (i == 0) primaryWaveLineWidth else secondaryWaveLineWidth
                val halfHeight = canvas.height / 2.toFloat()
                val width = canvas.width.toFloat()
                val mid = canvas.width / 2.toFloat()
                val maxAmplitude = halfHeight - 4.0f
                val progress = 1.0f - i.toFloat() / numberOfWaves
                val normedAmplitude = (1.5f * progress - 0.5f) * amplitude
                val path = Path()
                val multiplier = Math.min(1.0f, progress / 3.0f * 2.0f + 1.0f / 3.0f)
                var x = 0f
                while (x < width + density) {

                    // We use a parable to scale the sinus wave, that has its peak in the middle of the view.
                    val scaling = (-Math.pow(
                        1 / mid * (x - mid).toDouble(),
                        2.0
                    ) + 1).toFloat()
                    val y =
                        (scaling * maxAmplitude * normedAmplitude * Math.sin(2 * Math.PI * (x / width) * frequency + phase) + halfHeight).toFloat()
                    if (x == 0f) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    x += density
                }
                mPaintColor!!.style = Paint.Style.STROKE
                mPaintColor!!.isAntiAlias = true
                canvas.drawPath(path, mPaintColor!!)
                i++
            }
        } else {
            canvas.drawLine(
                5f,
                canvas.height / 2.toFloat(),
                canvas.width.toFloat(),
                canvas.height / 2.toFloat(),
                mPaintColor!!
            )
            canvas.drawLine(
                0f,
                canvas.height / 2.toFloat(),
                canvas.width.toFloat(),
                canvas.height / 2.toFloat(),
                mPaintColor!!
            )
            canvas.drawLine(
                -5f,
                canvas.height / 2.toFloat(),
                canvas.width.toFloat(),
                canvas.height / 2.toFloat(),
                mPaintColor!!
            )
        }
        phase += phaseShift
        invalidate()
    }

}