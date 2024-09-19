package br.com.pedroamarques.estoquista.factory

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import br.com.pedroamarques.estoquista.entities.ItemLeitura
import br.com.pedroamarques.estoquista.entities.Leitura
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ColetaExport(context: Context, leitura: Leitura, itens: List<ItemLeitura>) {

    private val mContext = context
    private val mLeitura: Leitura = leitura
    private val mItens: List<ItemLeitura> = itens
    private val FILE_PROVIDER = "br.com.pedroamarques.estoquista.fileprovider"

    fun geraArquivoUri(adapter: ColetaExportAdapter): Uri {
        val pasta = File(mContext.filesDir, "coletas")

        if(!pasta.exists()) {
            pasta.mkdirs()
        }

        Timber.e(mLeitura.dataInsercao.toString())

        val timeStamp = SimpleDateFormat("ddMMyyyy_HHmmss", Locale("pt", "BR")).format(mLeitura.dataInsercao?.time)
        val nomeArquivo = "Coleta_${mLeitura.id}_$timeStamp${adapter.extensao()}"
        val file = File(pasta, nomeArquivo)

        val cabecalho = adapter.cabecalho(mLeitura)
        val rodape = adapter.rodape(mLeitura)

        try {
            if(file.exists()) {
                file.delete()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        file.printWriter().use {
            if (!cabecalho.isEmpty()) {
                it.print(cabecalho)
            }

            for (item in mItens) {
                it.print(adapter.montaLinha(item))
            }

            if (!rodape.isEmpty()) {
                it.print(rodape)
            }
        }

        return FileProvider.getUriForFile(mContext, FILE_PROVIDER, file)
    }

    fun geraArquivo(adapter: ColetaExportAdapter): File {
        val pasta = File(mContext.filesDir, "coletas")

        if(!pasta.exists()) {
            pasta.mkdirs()
        }

        Timber.e(mLeitura.dataInsercao.toString())

        val timeStamp = SimpleDateFormat("ddMMyyyy_HHmmss", Locale("pt", "BR")).format(mLeitura.dataInsercao?.time)
        val nomeArquivo = "Coleta_${mLeitura.id}$timeStamp${adapter.extensao()}"
        val file = File(pasta, nomeArquivo)

        val cabecalho = adapter.cabecalho(mLeitura)
        val rodape = adapter.rodape(mLeitura)

        try {
            if(file.exists()) {
                file.delete()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        file.printWriter().use {
            if (!cabecalho.isEmpty()) {
                it.print(cabecalho)
            }

            for (item in mItens) {
                it.print(adapter.montaLinha(item))
            }

            if (!rodape.isEmpty()) {
                it.print(rodape)
            }
        }

        return file
    }

}