package br.com.pedroamarques.estoquista.helper

import android.text.TextUtils
import br.com.pedroamarques.estoquista.entities.ItemLeitura
import br.com.pedroamarques.estoquista.entities.Leitura
import br.com.pedroamarques.estoquista.factory.ColetaExportAdapter
import java.text.SimpleDateFormat
import java.util.*

class ColetaExportXML: ColetaExportAdapter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("pt", "BR"))

    override fun cabecalho(leitura: Leitura): String {
        var retorno = "<?xml version=\"1.0\" encoding=\"utf-8\"?><coleta><detalhes>"
        retorno = "$retorno<id>${leitura.id}</id>"
        retorno = "$retorno<titulo>${replaceCharInvalidos(leitura.titulo)}</titulo>"
        retorno = "$retorno<quantidade>${leitura.quantidade}</quantidade>"
        retorno = "$retorno<status>${leitura.status}</status>"
        retorno = "$retorno<data_insercao>${dateFormat.format(leitura.dataInsercao?.time)}</data_insercao>"
        retorno = "$retorno<data_alteracao>${dateFormat.format(leitura.dataAlteracao?.time)}</data_alteracao>"

        return "$retorno</detalhes><itens>"
    }

    override fun montaLinha(item: ItemLeitura): String {
        var retorno = "<item>"
        retorno = "$retorno<id>${item.id}</id>"
        retorno = "$retorno<codigo_barras>${item.codigoBarras}</codigo_barras>"
        retorno = "$retorno<quantidade>${item.quantidade}</quantidade>"
        retorno = "$retorno<data_hora_insercao>${dateFormat.format(item.dataInsercao?.time)}</data_hora_insercao>"
        retorno = "$retorno<data_alteracao>${dateFormat.format(item.dataAlteracao?.time)}</data_alteracao>"
        retorno = "$retorno<id_usuario_insercao>${item.idUsuarioInsercao}</id_usuario_insercao>"
        retorno = "$retorno<id_usuario_alteracao>${item.idUsuarioAlteracao}</id_usuario_alteracao>"
        retorno = "$retorno<tipo_leitura>${item.tipoLeitura ?: "L"}</tipo_leitura>"
        retorno = "$retorno<lote>${item.lote ?: ""}</lote>"

        return "$retorno</item>"
    }

    override fun rodape(leitura: Leitura): String {
        return "</itens></coleta>"
    }

    override fun mimetype(): String {
        return "text/xml"
    }

    override fun extensao(): String {
        return ".xml"
    }

    private fun replaceCharInvalidos(texto: String?): String {
        val mTexto = (texto ?: "").trim()

        if (mTexto.isEmpty()) {
            return ""
        }

        return mTexto.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}