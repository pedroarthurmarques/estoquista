package br.com.pedroamarques.estoquista.extensions

import br.com.pedroamarques.estoquista.extensions.CalendarExtensions.fimDia
import br.com.pedroamarques.estoquista.extensions.CalendarExtensions.inicioDia
import br.com.pedroamarques.estoquista.extensions.CalendarExtensions.primeiroDiaMes
import br.com.pedroamarques.estoquista.extensions.CalendarExtensions.ultimoDiaAno
import br.com.pedroamarques.estoquista.extensions.CalendarExtensions.ultimoDiaMes
import java.text.SimpleDateFormat
import java.util.*

object CalendarExtensions {
    val Calendar.primeiroDiaAno get() : Calendar {
        val cal = Calendar.getInstance()

        cal.set(this.get(Calendar.YEAR),
            this.get(Calendar.MONTH),
            this.get(Calendar.DATE),
            this.get(Calendar.HOUR),
            this.get(Calendar.MINUTE),
            this.get(Calendar.SECOND))

        cal.set(Calendar.MILLISECOND, this.get(Calendar.MILLISECOND))
        cal.set(Calendar.DAY_OF_YEAR, 1)

        return cal
    }

    val Calendar.ultimoDiaAno get() : Calendar {
        val cal = Calendar.getInstance()

        cal.set(this.get(Calendar.YEAR),
                this.get(Calendar.MONTH),
                this.get(Calendar.DATE),
                this.get(Calendar.HOUR),
                this.get(Calendar.MINUTE),
                this.get(Calendar.SECOND))

        cal.set(Calendar.MILLISECOND, this.get(Calendar.MILLISECOND))
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))

        return cal
    }

    val Calendar.primeiroDiaMes get() : Calendar {
        val cal = Calendar.getInstance()

        cal.set(this.get(Calendar.YEAR),
                this.get(Calendar.MONTH),
                this.get(Calendar.DATE),
                this.get(Calendar.HOUR),
                this.get(Calendar.MINUTE),
                this.get(Calendar.SECOND))

        cal.set(Calendar.MILLISECOND, this.get(Calendar.MILLISECOND))
        cal.set(Calendar.DAY_OF_MONTH, 1)

        return cal
    }

    val Calendar.ultimoDiaMes get() : Calendar {
        val cal = Calendar.getInstance()

        cal.set(this.get(Calendar.YEAR),
                this.get(Calendar.MONTH),
                this.get(Calendar.DATE),
                this.get(Calendar.HOUR),
                this.get(Calendar.MINUTE),
                this.get(Calendar.SECOND))

        cal.set(Calendar.MILLISECOND, this.get(Calendar.MILLISECOND))
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))

        return cal
    }

    val Calendar.inicioDia get() : Calendar {
        val cal = Calendar.getInstance()

        val year = this.get(Calendar.YEAR)
        val month = this.get(Calendar.MONTH)
        val day = this.get(Calendar.DATE)

        cal.set(year,month,day,0,0,0)

        return cal
    }

    val Calendar.fimDia get() : Calendar {
        val cal = Calendar.getInstance()

        val year = this.get(Calendar.YEAR)
        val month = this.get(Calendar.MONTH)
        val day = this.get(Calendar.DATE)

        cal.set(year,month,day,23,59,59)

        return cal
    }

    fun Calendar.toString(formato: String, locale: Locale = Locale("pt", "BR")): String {
        var retorno = ""
        try {
            val sdf =
                SimpleDateFormat(formato, locale)
            retorno = sdf.format(this.time)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return retorno
    }

    fun Calendar.toBasicDate(locale: Locale = Locale("pt", "BR")): String {
        var retorno = ""
        try {
            val sdf =
                SimpleDateFormat("dd/MM/yyyy", locale)
            retorno = sdf.format(this.time)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return retorno
    }

    fun Calendar.toDateDafault(locale: Locale = Locale("pt", "BR")): String {
        var retorno = ""
        try {
            val sdf =
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale)
            retorno = sdf.format(this.time)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return retorno
    }

    fun Calendar.toSQLite(locale: Locale = Locale("pt", "BR")): String {
        var retorno = ""
        try {
            val sdf =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
            retorno = sdf.format(this.time)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return retorno
    }

    fun Calendar.toMentor(locale: Locale = Locale("pt", "BR")): String {
        var retorno = ""
        try {
            val sdf =
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale)
            retorno = sdf.format(this.time)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return retorno
    }

    fun Calendar.adiciona(type: Int, amount: Int) : Calendar {
        val cal = Calendar.getInstance()
        cal.time = this.time

        cal.add(type, amount)

        return cal
    }

    fun adiciona(type: Int, amount: Int) : Calendar {
        val cal = Calendar.getInstance()
        cal.add(type, amount)

        return cal
    }
}