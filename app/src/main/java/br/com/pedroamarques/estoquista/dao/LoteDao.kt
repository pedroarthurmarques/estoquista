package br.com.pedroamarques.estoquista.dao

import androidx.room.*
import br.com.pedroamarques.estoquista.entities.Lote

@Dao
interface LoteDao {
    @Query("SELECT * FROM lote WHERE id_inventario_item = :id")
    fun getByIdInventarioItem(id: Long): List<Lote>

    @Query("SELECT * FROM lote WHERE id_lote = :id")
    fun getByID(id: Int): List<Lote>

    @Query("SELECT * FROM lote")
    fun getAll(): List<Lote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(lote: Lote): Long

    @Update
    fun update(lote: Lote)

    @Delete
    fun delete(lote: Lote)
}