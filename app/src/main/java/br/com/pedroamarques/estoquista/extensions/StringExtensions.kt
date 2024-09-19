package br.com.pedroamarques.estoquista.extensions

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object StringExtensions {
    val String?.removeNaoAlphaNumericos get() : String? = if (this.isNullOrEmpty()) "" else this.trim { it <= ' ' }.replace("[^A-Za-z0-9]".toRegex(), "")
    val String?.removeNaoNumericos get() : String? = if (this.isNullOrEmpty()) "" else this.trim { it <= ' ' }.replace("[^0-9]".toRegex(), "")
    val String?.isEmailValid get() : Boolean = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(this).find()

    val String?.isCPFValid get() : Boolean {
        if (this.isNullOrEmpty()) return false

        val numbers = arrayListOf<Int>()

        this.filter { it.isDigit() }.forEach {
            numbers.add(it.toString().toInt())
        }

        if (numbers.size != 11) return false

        //repeticao
        (0..9).forEach { n ->
            val digits = arrayListOf<Int>()
            (0..10).forEach { digits.add(n) }
            if (numbers == digits) return false
        }

        //digito 1
        val dv1 = ((0..8).sumBy { (it + 1) * numbers[it] }).rem(11).let {
            if (it >= 10) 0 else it
        }

        val dv2 = ((0..8).sumBy { it * numbers[it] }.let { (it + (dv1 * 9)).rem(11) }).let {
            if (it >= 10) 0 else it
        }

        return numbers[9] == dv1 && numbers[10] == dv2
    }

    val String?.isCNPJValid get() : Boolean {
        if (this.isNullOrEmpty()) return false

        val numbers = arrayListOf<Int>()

        this.filter { it.isDigit() }.forEach {
            numbers.add(it.toString().toInt())
        }

        if (numbers.size != 14) return false

        //repeticao
        (0..9).forEach { n ->
            val digits = arrayListOf<Int>()
            (0..14).forEach { digits.add(n) }
            if (numbers == digits) return false
        }

        //digito 1
        val dv1 = 11 - (arrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2).mapIndexed { index, i ->
            i * numbers[index]
        }).sum().rem(11)
        numbers.add(dv1)

        //digito 2
        val dv2 = 11 - (arrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2).mapIndexed { index, i ->
            i * numbers[index]
        }).sum().rem(11)

        return numbers[12] == dv1 && numbers[13] == dv2
    }

    val String?.isCpfCnpjValid get() : Boolean {
        if (this.isNullOrEmpty()) {
            return false
        }

        return when (this.length) {
            11 -> {
                this.isCPFValid
            }
            14 -> {
                this.isCNPJValid
            }
            else -> {
                false
            }
        }
    }

    val String?.formatCPF get() : String? {
        var cpf = this
        if (cpf != null && cpf.length == 11) {
            cpf = cpf.substring(0, 3) + "." +
                    cpf.substring(3, 6) + "." +
                    cpf.substring(6, 9) + "-" +
                    cpf.substring(9, 11)
        }
        return cpf
    }

    val String?.formatCNPJ get() : String? {
        var cnpj = this
        if (cnpj != null && cnpj.length == 14) {
            cnpj = cnpj.substring(0, 2) + "." +
                    cnpj.substring(2, 5) + "." +
                    cnpj.substring(5, 8) + "/" +
                    cnpj.substring(8, 12) + "-" +
                    cnpj.substring(12)
        }
        return cnpj
    }

    val String?.formatCpfCnpj get() : String? {
        val cpfOuCnpj = this

        if (cpfOuCnpj != null && cpfOuCnpj.length == 14) return cpfOuCnpj.formatCNPJ
        else if (cpfOuCnpj != null && cpfOuCnpj.length == 11) return cpfOuCnpj.formatCPF

        return cpfOuCnpj
    }

    val String?.formatCEP get() : String? {
        var cep = this
        if (cep != null && cep.length == 8) {
            cep = cep.substring(0, 5) + "-" + cep.substring(5, 8)
        }
        return cep
    }

    val String?.formatPhone get() : String? {
        var str = this
        if (!str.isNullOrEmpty()) {
            if (str.length == 11) {
                str = "(" +
                        str.substring(0, 2) + ")" +
                        str.substring(2, 7) + "-" +
                        str.substring(7)

            } else if (str.length == 10) {
                str = "(" +
                        str.substring(0, 2) + ")" +
                        str.substring(2, 6) + "-" +
                        str.substring(6)
            }
        }
        return str
    }

    val String?.capitalizaPalavras get() : String {
        val s = this

        if (s.isNullOrEmpty()) {
            return ""
        }
        val palavras = s.split(" ").toTypedArray()
        val sb = StringBuilder()
        for (palavra in palavras) {
            if (palavra.length > 0) sb.append(Character.toUpperCase(palavra[0]))
            if (palavra.length > 1) sb.append(palavra.substring(1).toLowerCase())
            sb.append(" ")
        }
        return sb.toString().trim { it <= ' ' }
    }

    val String?.capitalizaPrimeiro get() : String? {
        val s = this

        return if (s.isNullOrEmpty()) {
            return s
        } else {
            val first = s.firstOrNull()?.uppercase()
            val dropFirst = s.substring(1)

            "$first$dropFirst"
        }
    }

    val String?.primeiraPalavra get() : String? {
        return if (this.isNullOrEmpty() || !this.contains(" ")) {
            this
        } else this.split("\\s+").toTypedArray()[0]
    }

    val String?.isValidCep get() : Boolean {
        return this.removeNaoNumericos?.length == 8
    }

    val String?.iniciais get() : String? {
        if (this.isNullOrEmpty()) {
            return null
        }

        if (!this.contains(" ")) {
            return this.substring(0, 1)
        }

        val splits = this
            .split(' ')
            .mapNotNull { it.firstOrNull()?.toString() }

        return "${splits.firstOrNull()}${splits.lastOrNull()}".trim()
    }

    val String?.isNumber get() : Boolean {
        return this?.toIntOrNull() != null
    }



    val String?.isValidUrl get() : Boolean {
        if (this.isNullOrBlank()) {
            return false
        }

        val urlPattern = "^(http|https)://.*".toRegex()

        if (urlPattern.matches(this)) {
            try {
                val uri = java.net.URI(this)
                return !uri.host.isNullOrEmpty()
            } catch (e: Exception) {
                return false
            }
        }

        return false
    }

    fun String?.toCalendar(formato: String, locale: Locale = Locale("pt", "BR")): Calendar? {
        if (this.isNullOrEmpty()) {
            return null
        }

        return try {
            val dtFormat = SimpleDateFormat(formato, locale)
            val calendar = Calendar.getInstance(locale)
            calendar.time = dtFormat.parse(this)

            calendar

        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun String?.toMentorCalendar(locale: Locale = Locale("pt", "BR")): Calendar? {
        if (this.isNullOrEmpty()) {
            return null
        }

        return try {
            val dtFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale)
            val calendar = Calendar.getInstance(locale)
            calendar.time = dtFormat.parse(this)

            calendar

        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun String?.toSQLiteCalendar(locale: Locale = Locale("pt", "BR")): Calendar? {
        if (this.isNullOrEmpty()) {
            return null
        }

        return try {
            val dtFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
            val calendar = Calendar.getInstance(locale)
            calendar.time = dtFormat.parse(this)

            calendar

        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun String?.isValidDate(formato: String = "yyyy-MM-dd HH:mm:ss") : Boolean {
        try {
            SimpleDateFormat(formato, Locale.getDefault()).parse(this)
            return true

        } catch (ex: ParseException) {
            ex.printStackTrace()
        }

        return false
    }

    fun String?.reformataData(deFormato: String, paraFormato: String, locale: Locale = Locale("pt", "BR")): String? {
        if (this.isNullOrEmpty()) {
            return null
        }

        return try {
            val dtFormat1 = SimpleDateFormat(deFormato, locale)
            val dtFormat2 = SimpleDateFormat(paraFormato, locale)

            return dtFormat2.format(dtFormat1.parse(this))

        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun String?.reformataDataDeSQLite(paraFormato: String, locale: Locale = Locale("pt", "BR")): String? {
        if (this.isNullOrEmpty()) {
            return null
        }

        return try {
            val dtFormat1 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
            val dtFormat2 = SimpleDateFormat(paraFormato, locale)

            return dtFormat2.format(dtFormat1.parse(this))

        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun String?.reformataDataDeMentor(paraFormato: String, locale: Locale = Locale("pt", "BR")): String? {
        if (this.isNullOrEmpty()) {
            return null
        }

        return try {
            val dtFormat1 = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", locale)
            val dtFormat2 = SimpleDateFormat(paraFormato, locale)

            return dtFormat2.format(dtFormat1.parse(this))

        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun String?.reformataDataParaSQLite(deFormato: String, locale: Locale = Locale("pt", "BR")): String? {
        if (this.isNullOrEmpty()) {
            return null
        }

        return try {
            val dtFormat1 = SimpleDateFormat(deFormato, locale)
            val dtFormat2 = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)

            return dtFormat2.format( dtFormat1.parse(this))

        } catch (ex: Exception) {
            null
        }
    }

    fun String.toHtmlSpan(): Spanned = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
}