package br.com.pedroamarques.estoquista.helper

import android.content.Context
import br.com.pedroamarques.estoquista.BuildConfig
import br.com.pedroamarques.estoquista.extensions.StringExtensions.removeNaoAlphaNumericos

object Const {
    object Preference {
        const val APP_PREFERENCE = "APP_PREFERENCE"
        const val LEITURA_PADRAO = "LEITURA_PADRAO"
        const val BARCODE_PADRAO = "BARCODE_PADRAO"
    }

    object Leitor {
        enum class TiposLeitura { Manual, Pistola, Escaner, CNPJ, Numero }
        enum class ModoLeitura { Sequencial, Contagem }
        enum class BarcodeType { Ean13, Ean8, UpcA, UpcE, Code128, Code39, Code93, Itf, Rss14, RssExpended }

        data class Barcode (val nome: String, val tipo: BarcodeType)

        val FORMATOS = arrayListOf<Barcode>(
            Barcode("EAN 13", BarcodeType.Ean13),
            Barcode("EAN 8", BarcodeType.Ean8),
            Barcode("UPC-A", BarcodeType.UpcA),
            Barcode("UPC-E", BarcodeType.UpcE),
            Barcode("CODE 128", BarcodeType.Code128),
            Barcode("CODE 39", BarcodeType.Code39),
            Barcode("CODE 93", BarcodeType.Code93),
            Barcode("ITF", BarcodeType.Itf),
            Barcode("RSS-14", BarcodeType.Rss14),
            Barcode("RSS-EXPANDED", BarcodeType.RssExpended)
        )
    }
}