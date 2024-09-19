package br.com.pedroamarques.estoquista.helper

import android.text.TextUtils
import br.com.pedroamarques.estoquista.entities.ItemLeitura
import br.com.pedroamarques.estoquista.entities.Leitura
import br.com.pedroamarques.estoquista.factory.ColetaExportAdapter
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ColetaExportJSON: ColetaExportAdapter {
    private var primeiraLinha = true
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("pt", "BR"))

    override fun cabecalho(leitura: Leitura): String {
        val jsonDados = JSONObject()

        jsonDados.put("id", leitura.id)
        jsonDados.put("titulo", leitura.titulo)
        jsonDados.put("quantidade", leitura.quantidade)
        jsonDados.put("status", leitura.status)
        jsonDados.put("data_insercao", dateFormat.format(leitura.dataInsercao?.time))
        jsonDados.put("data_alteracao", dateFormat.format(leitura.dataAlteracao?.time))

        return "{\"detalhes\":$jsonDados,\"itens\":["
    }

    override fun montaLinha(item: ItemLeitura): String {
        val jsonDados = JSONObject()
        jsonDados.put("id", item.id)
        jsonDados.put("codigo_barras", item.codigoBarras)
        jsonDados.put("quantidade", item.quantidade)
        jsonDados.put("data_hora_insercao", dateFormat.format(item.dataInsercao?.time))
        jsonDados.put("data_hora_alteracao", dateFormat.format(item.dataAlteracao?.time))
        jsonDados.put("id_usuario_insercao", item.idUsuarioInsercao)
        jsonDados.put("id_usuario_alteracao", item.idUsuarioAlteracao)

        if (!TextUtils.isEmpty(item.tipoLeitura)) {
            jsonDados.put("tipo_leitura", item.tipoLeitura)
        }

        if (!TextUtils.isEmpty(item.lote)) {
            jsonDados.put("lote", item.lote)
        }

        if(primeiraLinha) {
            primeiraLinha = false
            return "$jsonDados"

        } else {
            return ",$jsonDados"
        }
    }

    override fun rodape(leitura: Leitura): String {
        return "]}"
    }

    override fun mimetype(): String {
        return "application/json"
    }

    override fun extensao(): String {
        return ".json"
    }
}