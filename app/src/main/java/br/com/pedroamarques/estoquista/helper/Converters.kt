package br.com.pedroamarques.estoquista.helper

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class Converters {
    @TypeConverter
    fun fromString(value: String?): Calendar? {
        if(value == null || value.isNullOrEmpty()) {
            return null
        }

        val locale = Locale("pt-BR")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale)
        val calendar = Calendar.getInstance(locale)

        try {
            calendar.time = dateFormat.parse(value)
        } catch (ex: Exception) {
            return null
        }

        return calendar

    }
    @TypeConverter
    fun calendarToString(calendar: Calendar?): String? {
        if(calendar == null) {
            return null
        }

        val locale = Locale("pt-BR")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale)

        return dateFormat.format(calendar.time)
    }

    @TypeConverter
    fun bigDecimalToDouble(input: BigDecimal?): Double? {
        return input?.toDouble()
    }

    @TypeConverter
    fun doubleToBigDecimal(input: Double?): BigDecimal? {
        if (input == null) return null

        return try { BigDecimal.valueOf(input) } catch (ex: Exception) { BigDecimal.ZERO }
    }
}