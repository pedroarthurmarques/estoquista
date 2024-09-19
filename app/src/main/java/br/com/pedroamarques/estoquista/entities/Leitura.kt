package br.com.pedroamarques.estoquista.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "leituras")
class Leitura {
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    @ColumnInfo(name = "id")
    var id: Long = 0

    @SerializedName("titulo")
    @ColumnInfo(name = "titulo")
    var titulo: String? = null

    @SerializedName("quantidade")
    @ColumnInfo(name = "quantidade")
    var quantidade: Int = 0

    @SerializedName("data_insercao")
    @ColumnInfo(name = "data_insercao")
    var dataInsercao: Calendar? = null

    @SerializedName("data_alteracao")
    @ColumnInfo(name = "data_alteracao")
    var dataAlteracao: Calendar? = null

    /*
    * P: Pendente, F: Finalizado, E: Excluído
    * */
    @SerializedName("status")
    @ColumnInfo(name = "status")
    var status: String? = null

    override fun toString(): String {
        return "{id = $id, titulo = $titulo, quantidade = $quantidade, dataInsercao = $dataInsercao, dataAlteracao = $dataAlteracao, status = $status}"
    }
}