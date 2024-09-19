package br.com.pedroamarques.estoquista.factory

import br.com.pedroamarques.estoquista.entities.ItemLeitura
import br.com.pedroamarques.estoquista.entities.Leitura

interface ColetaExportAdapter {
    fun cabecalho(leitura: Leitura): String
    fun montaLinha(item: ItemLeitura): String
    fun rodape(leitura: Leitura): String
    fun mimetype(): String
    fun extensao(): String
}