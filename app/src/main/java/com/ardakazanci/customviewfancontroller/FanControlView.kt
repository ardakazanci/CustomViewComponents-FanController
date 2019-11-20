package com.ardakazanci.customviewfancontroller

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.withStyledAttributes
import java.lang.Double.min
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private enum class FanSpeed(val label: Int) {

    // Fan Kontrolü üzerinde bulunan değerlerin tanımlanması.
    OFF(R.string.fan_off), // off
    LOW(R.string.fan_low), // 1
    MEDIUM(R.string.fan_medium), // 2
    HIGH(R.string.fan_high); // 3

    fun next() = when (this) {
        OFF -> LOW
        LOW -> MEDIUM
        MEDIUM -> HIGH
        HIGH -> OFF
    }

}

private const val RADIUS_OFFSET_LABEL = 30
private const val RADIUS_OFFSET_INDICATOR = -35


class FanControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    private var fanSpeedLowColor = 0
    private var fanSpeedMediumColor = 0
    private var fanSeedMaxColor = 0

    /**
     * Çizim işlemlerine başlamadan önce varsayılan değerler belirledik.
     *
     * Circle Yarıçap değeri
     * Başlangıç Fan Kontrol değeri
     * Pozisyon bilgisi değeri PointF(x,y)
     */
    private var radius = 0.0f // Radius of the circle.
    private var fanSpeed = FanSpeed.OFF // The active selection.
    private val pointPosition: PointF = PointF(0.0f, 0.0f)

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.FanControlView) {
            fanSpeedLowColor = getColor(R.styleable.FanControlView_fanColor1, 0)
            fanSpeedMediumColor = getColor(R.styleable.FanControlView_fanColor2, 0)
            fanSeedMaxColor = getColor(R.styleable.FanControlView_fanColor3, 0)
        }
    }


    override fun performClick(): Boolean {
        if (super.performClick()) return true

        fanSpeed = fanSpeed.next()
        contentDescription = resources.getString(fanSpeed.label)

        invalidate()
        return true
    }


    // Özel görünümün ölçeğini hesaplar.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        /**
         * Radius değeri varsayılan yarıçap değerini verir.
         * min(1024,768) / 2.0 * 0.8 = 768 / 2.0 * 0.8
         */
        radius = (min(w, h) / 5.0 * 0.8).toFloat()

    }


    private fun PointF.computeXYForSpeed(pos: FanSpeed, radius: Float) {
        // Angles are in radians.

        /**
         *  pos : 1 , 2 , 3 , off  0 degree 25 degree 50 degree 75 degree 100 degree
         *  radius : 10
         */

        val startAngle = Math.PI * (9 / 8.0) // Başlangıç Açı değeri
        val angle = startAngle + pos.ordinal * (Math.PI / 4) // Açı değeri
        // ilgili açı değerine göre pos değerleri yerleştirilecek
        x = (radius * cos(angle)).toFloat() + width / 2
        y = (radius * sin(angle)).toFloat() + height / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        paint.color = when (fanSpeed) {
            FanSpeed.OFF -> Color.GRAY
            FanSpeed.LOW -> fanSpeedLowColor
            FanSpeed.MEDIUM -> fanSpeedMediumColor
            FanSpeed.HIGH -> fanSeedMaxColor
        } as Int

        // Set dial background color to green if selection not off.


        canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)

        // 1 2 3 off değerlerinin gösterileceği konum ayarlaması
        val markerRadius = radius + RADIUS_OFFSET_INDICATOR
        pointPosition.computeXYForSpeed(fanSpeed, markerRadius)
        paint.color = Color.BLACK
        canvas?.drawCircle(pointPosition.x, pointPosition.y, radius / 12, paint)

        // 1 2 3 off değerlerinin açıya göre yazdırılması
        val labelRadius = radius + RADIUS_OFFSET_LABEL
        for (i in FanSpeed.values()) {
            pointPosition.computeXYForSpeed(i, labelRadius)
            val label = resources.getString(i.label)
            canvas?.drawText(label, pointPosition.x, pointPosition.y, paint)
        }


    }

    /**
     * Çizim işlemlerine başlamadan önce varsayılan stil değerleri belirledik.
     */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }


}