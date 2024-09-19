package br.com.pedroamarques.estoquista.helper

import android.text.TextUtils
import br.com.pedroamarques.estoquista.entities.ItemLeitura
import br.com.pedroamarques.estoquista.entities.Leitura
import br.com.pedroamarques.estoquista.factory.ColetaExportAdapter

class ColetaExportTXTGenerico: ColetaExportAdapter {
    private var primeiraLinha = true

    override fun cabecalho(leitura: Leitura): String {
        //return "#,EAN,QUANTIDADE"
        return ""
    }

    override fun montaLinha(item: ItemLeitura): String {
        var linha = ""

        if(primeiraLinha) {
            primeiraLinha = false
            linha = "${item.codigoBarras},${item.quantidade},${item.tipoLeitura ?: "L"},${item.lote ?: ""}"

        } else {
            linha = "${System.getProperty("line.separator")}${item.codigoBarras},${item.quantidade},${item.tipoLeitura ?: "L"},${item.lote ?: ""}"
        }

        return linha
    }

    override fun rodape(leitura: Leitura): String {
        return ""
    }

    override fun mimetype(): String {
        return "text/plain"
    }

    override fun extensao(): String {
        return ".txt"
    }
}