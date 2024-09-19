package br.com.pedroamarques.estoquista.helper

import br.com.pedroamarques.estoquista.entities.ItemLeitura

object LeituraUtils {
    fun contaItens(itens: List<ItemLeitura>?): Int {
        if (itens == null) {
            return 0
        }

        var total = 0
        for (item in itens) {
            total += item.quantidade
        }

        return total
    }
}