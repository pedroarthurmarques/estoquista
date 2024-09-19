package br.com.pedroamarques.estoquista.extensions

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object NumberExtensions {

    fun Float.toPx(context: Context): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)

    fun Float.toDp(context: Context): Float = (this/(context.resources.displayMetrics.densityDpi/ DisplayMetrics.DENSITY_DEFAULT))

    fun Int.toPx(context: Context): Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()

    fun Int.toDp(context: Context): Int = (this/(context.resources.displayMetrics.density)).toInt()
    fun BigDecimal.formatValor(casasDecimais: Int = 2) : String {
        return try {
            val locale = Locale.getDefault()
            val format = NumberFormat.getCurrencyInstance(locale)
            format.roundingMode = RoundingMode.HALF_EVEN
            format.minimumFractionDigits = casasDecimais
            format.maximumFractionDigits = casasDecimais

            val simboloMoeda = Currency.getInstance(locale).getSymbol(locale)
            format.format(this).replace(simboloMoeda, "")

        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            this.toString()
        }
    }
    fun BigDecimal.formatMoney(casasDecimais: Int = 2) : String {
        return try {
            val locale = Locale.getDefault()
            val format = NumberFormat.getCurrencyInstance(locale)
            format.roundingMode = RoundingMode.HALF_EVEN
            format.minimumFractionDigits = casasDecimais
            format.maximumFractionDigits = casasDecimais

            format.format(this)

        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            this.toString()
        }
    }
    fun Double.roundDecimal(mode: RoundingMode = RoundingMode.CEILING, pattern: String = "#.##"): Double {
        val df = DecimalFormat(pattern)
        df.roundingMode = mode
        return df.format(this).toDouble()
    }
    fun Float.roundDecimal(mode: RoundingMode = RoundingMode.CEILING, pattern: String = "#.##"): Float {
        val df = DecimalFormat(pattern)
        df.roundingMode = mode
        return df.format(this).toFloat()
    }
}