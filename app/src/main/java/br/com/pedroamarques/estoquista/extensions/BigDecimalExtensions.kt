package br.com.pedroamarques.estoquista.extensions

import timber.log.Timber
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

object BigDecimalExtensions {
    fun BigDecimal?.parseString(decSeparator: Char = '.', casasDecimais: Int = 1, trunc: Boolean = false) : String? {
        var str: String? = null

        this?.let { bd ->
            try {
                val symbols = DecimalFormatSymbols()
                symbols.decimalSeparator = decSeparator

                val charPattern: String = if (trunc) "#" else "0"
                var pattern = charPattern

                if (casasDecimais > 0) {
                    pattern += "."
                    for (i in 1..casasDecimais) {
                        pattern += charPattern
                    }
                }

                val decimalFormat = DecimalFormat(pattern, symbols)

                str = decimalFormat.format(bd)

            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return str
    }
}