package br.com.pedroamarques.estoquista.helper

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import timber.log.Timber

import java.net.NetworkInterface
import java.util.*

object Dispositivo {

    /**
     * Retorna marca e modelo do aparelho
     * @return string contendo a marca e o modelo do aparelho
     */
    val modelo: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

    /**
     * Retorna o sistema operacional
     * @return sempre retorna "Android"
     */
    val os: String
        get() = "Android"

    /**
     * Retorna a versão do aplicativo declarada no gradle
     * @return string contendo a versão do aplicativo
     */
    val versaoOS: String
        get() {
            val sb = StringBuilder()
            sb.append(Build.VERSION.RELEASE)
            return sb.toString()
        }

    /**
     * Retorna o AppID do aplicativo
     * @param context contexto do aplicativo
     * @return string contendo app id
     */
    fun getAppID(context: Context): String {
        return context.packageName
    }

    /**
     * Retorna a versão do aplicativo
     * @param context contexto do aplicativo
     * @return string contendo a versão do aplicativo ou "Não encontrado"
     */
    fun getVersaoApp(context: Context): String {
        try {
            val pi = context.packageManager.getPackageInfo(context.packageName, 0)
            return pi.versionName

        } catch (ex: PackageManager.NameNotFoundException) {
            Timber.e(ex.localizedMessage)
            return "Não encontrado"
        }

    }

    /**
     * Retorna identificador único do android
     * @param context contexto do aplicativo
     * @return string contendo o identificador único do aplicativo
     */
    fun getUDID(context: Context): String {
        return android.provider.Settings.System.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    /**
     * Retorna IP da rede conectada desde que não seja localhost
     * @param usarIPV4  true=retorna ipv4, false=retorna ipv6
     * @return  endereço IP ou string vazia
     */
    fun getIP(usarIPV4: Boolean): String {
        try {
            val locale = Locale("pt", "BR")
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress

                        val isIPv4 = sAddr.indexOf(':') < 0

                        if (usarIPV4) {
                            if (isIPv4)
                                return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // dropa zona ip6 do sufixo
                                return if (delim < 0) sAddr.toUpperCase(locale) else sAddr.substring(
                                    0,
                                    delim
                                ).toUpperCase(locale)
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Timber.e(ex.localizedMessage)
        }
        // se chegou aqui, que coma exceptions
        return ""
    }

    // retorna primeira letra em maiusculo
    private fun capitalize(s: String?): String {
        if (s == null || s.length == 0) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

}
