package br.com.pedroamarques.estoquista.dao

import androidx.room.*
import br.com.pedroamarques.estoquista.entities.ItemLeitura
import br.com.pedroamarques.estoquista.entities.Leitura

@Dao
interface ItemLeituraDao {
    @Query("""SELECT * 
                FROM itens_leituras 
               WHERE id_leitura = :idLeitura 
            ORDER BY id DESC""")
    fun getAllByLeituraID(idLeitura: Int): List<ItemLeitura>

    @Query("""SELECT * 
                FROM itens_leituras 
               WHERE codigo_barras = :codigoBarras 
                 AND id_leitura = :idLeitura
                 AND (lote = :lote or (lote is null and :lote is null)) 
            ORDER BY CASE WHEN :orderByDesc = 1 THEN id END DESC, 
                     CASE WHEN :orderByDesc = 0 THEN id END ASC""")
    fun getAllByCodigoBarras(codigoBarras: String?, idLeitura: Int, lote: String? = null, orderByDesc: Boolean = true): List<ItemLeitura>

    @Query("""SELECT * 
                FROM itens_leituras 
               WHERE id_leitura = :idLeitura
                 AND lote = :lote
            ORDER BY id DESC""")
    fun getAllByLote(idLeitura: Int, lote: String): List<ItemLeitura>

    @Query("""
        SELECT MAX(id) as id
              ,id_leitura
              ,codigo_barras
              ,sum(quantidade) as quantidade 
              ,mensagem_erro
              ,max(data_hora_insercao) as data_hora_insercao
              ,max(data_hora_alteracao) as data_hora_alteracao
              ,max(id_usuario_insercao) as id_usuario_insercao
              ,max(id_usuario_alteracao) as id_usuario_alteracao
              ,lote
              ,controla_lotes
              ,max(sequencia) as sequencia
          FROM itens_leituras 
         WHERE id_leitura = :idLeitura
         GROUP BY id_leitura
                 ,codigo_barras
                 ,mensagem_erro
                 ,lote
                 ,controla_lotes
         ORDER BY MAX(sequencia) DESC
    """)
    fun getAllAgrupadoByQuantidade(idLeitura: Int): List<ItemLeitura>

    @Query("""
        SELECT MAX(id) as id
              ,id_leitura
              ,codigo_barras
              ,tipo_leitura
              ,sum(quantidade) as quantidade 
              ,max(data_hora_insercao) as data_hora_insercao
              ,max(data_hora_alteracao) as data_hora_alteracao
              ,max(id_usuario_insercao) as id_usuario_insercao
              ,max(id_usuario_alteracao) as id_usuario_alteracao
              ,lote
              ,controla_lotes
              ,max(sequencia) as sequencia
          FROM itens_leituras 
         WHERE id_leitura = :idLeitura
         GROUP BY id_leitura
                 ,codigo_barras
                 ,tipo_leitura
                 ,lote
                 ,controla_lotes
         ORDER BY MAX(sequencia) DESC
    """)
    fun getAllAgrupadoByTipoLeitura(idLeitura: Long): List<ItemLeitura>

    @Query("SELECT * FROM itens_leituras WHERE id = :id")
    fun getByID(id: Int): List<ItemLeitura>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(itemLeitura: ItemLeitura): Long

    @Delete
    fun delete(itemLeitura: ItemLeitura)

    @Update
    fun update(itemLeitura: ItemLeitura)

    @Query("""DELETE FROM itens_leituras
                    WHERE codigo_barras = :codigoBarras 
                      and id_leitura = :idLeitura 
                      and (lote = :lote or (lote is null and :lote is null))""")
    fun deleteCodigoBarras(codigoBarras: String, idLeitura: Int, lote: String? = null)

    @Query("""UPDATE itens_leituras
                 SET sequencia = :sequencia
              WHERE id_leitura = :idLeitura
                AND codigo_barras = :codigoBarras 
                AND (lote = :lote or (lote is null and :lote is null))""")
    fun updateSequencia(sequencia: Int,  idLeitura: Long?, codigoBarras: String, lote: String?)

    @Query("""SELECT * 
                FROM itens_leituras 
               WHERE id_leitura = :idLeitura
                 AND codigo_barras = :codigo
                 AND lote = :serial""")
    fun consultaSerial(idLeitura: Long?, codigo: String?, serial: String?): List<ItemLeitura>

    @Query("""SELECT ifnull(max(sequencia), 0)+1 FROM itens_leituras  WHERE id_leitura = :idLeitura""")
    fun nextSequencia(idLeitura: Long?): Int
}