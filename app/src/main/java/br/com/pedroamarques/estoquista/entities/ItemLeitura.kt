package br.com.pedroamarques.estoquista.entities

import androidx.room.*
import br.com.pedroamarques.estoquista.extensions.CalendarExtensions.toString
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "itens_leituras",
        foreignKeys = arrayOf(
            ForeignKey(entity = Leitura::class,
                        parentColumns = arrayOf("id"),
                        childColumns = arrayOf("id_leitura"),
                        onDelete = ForeignKey.CASCADE)
            )
        )
data class ItemLeitura (
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    @ColumnInfo(name = "id", index = true)
    var id: Long = 0,

    @SerializedName("id_leitura")
    @ColumnInfo(name = "id_leitura", index = true)
    var idLeitura: Long = 0,

    /*
    * M: Manual, C: Câmera, L: Leitor
    * */
    @SerializedName("tipo_leitura")
    @ColumnInfo(name = "tipo_leitura")
    var tipoLeitura: String? = null,

    @SerializedName("mensagem_erro")
    @ColumnInfo(name = "mensagem_erro")
    var mensagemErro: String? = null,

    @SerializedName("codigo_barras")
    @ColumnInfo(name = "codigo_barras")
    var codigoBarras: String? = null,

    @ColumnInfo(name = "quantidade")
    @SerializedName("quantidade")
    var quantidade: Int = 0,

    @SerializedName("data_hora_insercao")
    @ColumnInfo(name = "data_hora_insercao")
    var dataInsercao: Calendar? = null,

    @SerializedName("data_hora_alteracao")
    @ColumnInfo(name = "data_hora_alteracao")
    var dataAlteracao: Calendar? = null,

    @SerializedName("id_usuario_insercao")
    @ColumnInfo(name = "id_usuario_insercao")
    var idUsuarioInsercao: Long = 0,

    @SerializedName("id_usuario_alteracao")
    @ColumnInfo(name = "id_usuario_alteracao")
    var idUsuarioAlteracao: Long = 0,

    /*
    * S: Serial, L: Lotes, N: Normal
    * */
    @SerializedName("controla_lotes")
    @ColumnInfo(name = "controla_lotes")
    var controlaLotes: String = "N",

    @SerializedName("lote")
    @ColumnInfo(name = "lote")
    var lote: String? = null,

    @ColumnInfo(name = "sequencia")
    @SerializedName("sequencia")
    var sequencia: Int = 0
) {
    override fun toString(): String {
        return "{id = $id, idLeitura = $idLeitura, codigoBarras = $codigoBarras, quantidade = $quantidade, dataInsercao = ${dataInsercao.toString()}, dataAlteracao = ${dataAlteracao.toString()}, idUsuarioInsercao = $idUsuarioInsercao, idUsuarioAlteracao = $idUsuarioAlteracao, controlaLotes = $controlaLotes, lote = $lote, sequencia = $sequencia}"
    }
}