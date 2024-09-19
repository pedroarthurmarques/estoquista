package br.com.pedroamarques.estoquista.views

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config
import android.graphics.PorterDuff.Mode
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet

class RoundedImageView : androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onDraw(canvas: Canvas) {

        val drawable = getDrawable() ?: return

        if (getWidth() === 0 || getHeight() === 0) {
            return
        }
        val b = (drawable as BitmapDrawable).bitmap
        val bitmap = b.copy(Config.ARGB_8888, true)

        val w = getWidth()
        val h = getHeight()

        val roundBitmap = getCroppedBitmap(bitmap, w)
        canvas.drawBitmap(roundBitmap, 0f, 0f, null)

    }

    companion object {

        fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
            val sbmp: Bitmap

            if (bmp.width != radius || bmp.height != radius) {
                val smallest = Math.min(bmp.width, bmp.height).toFloat()
                val factor = smallest / radius
                sbmp =
                    Bitmap.createScaledBitmap(bmp, (bmp.width / factor).toInt(), (bmp.height / factor).toInt(), false)
            } else {
                sbmp = bmp
            }

            val output = Bitmap.createBitmap(
                radius, radius,
                Config.ARGB_8888
            )
            val canvas = Canvas(output)

            val color = -0x5e688c
            val paint = Paint()
            val rect = Rect(0, 0, radius, radius)

            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = Color.parseColor("#BAB399")
            canvas.drawCircle(
                radius / 2 + 0.7f,
                radius / 2 + 0.7f, radius / 2 + 0.1f, paint
            )
            paint.xfermode = PorterDuffXfermode(Mode.SRC_IN)
            canvas.drawBitmap(sbmp, rect, rect, paint)

            return output
        }
    }

}