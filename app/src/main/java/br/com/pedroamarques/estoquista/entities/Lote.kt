package br.com.pedroamarques.estoquista.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "lote")
data class Lote (
    @Expose(serialize = false, deserialize = false)
    @PrimaryKey
    @ColumnInfo(name = "id_lote", index = true)
    @SerializedName("id_lote")
    var idLote: Long = 0,

    @SerializedName("id_inventario_item")
    @ColumnInfo(name = "id_inventario_item", index = true)
    var idInventarioItem: Long = 0,

    @SerializedName("id_inventario_item_lote")
    @ColumnInfo(name = "id_inventario_item_lote")
    var idInventarioItemLote: Long = 0,

    @SerializedName("lote")
    @ColumnInfo(name = "lote")
    var lote: String? = null,

    @SerializedName("data_vencimento")
    @ColumnInfo(name = "data_vencimento")
    var dataVencimento: Calendar? = null,

    @SerializedName("fabricdata_fabricacaoacao")
    @ColumnInfo(name = "fabricdata_fabricacaoacao")
    var fabricDataFabricacaoAcao: Calendar? = null,

    @SerializedName("quantidade_estoque")
    @ColumnInfo(name = "quantidade_estoque")
    var quantidadeEstoque: Double = 0.0
)