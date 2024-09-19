package br.com.pedroamarques.estoquista.dao

import androidx.room.*
import br.com.pedroamarques.estoquista.entities.Leitura

@Dao
interface LeituraDao {
    @Query("""
        SELECT l.* 
          FROM leituras l 
         WHERE l.status != 'E'
         ORDER BY l.id DESC
        """)
    fun getAll(): List<Leitura>

    @Query("""
        SELECT l.* 
          FROM leituras l 
         WHERE l.status = 'E'
         ORDER BY l.id DESC
        """)
    fun getLixeira(): List<Leitura>

    @Query("""SELECT * 
                      FROM leituras l
                     WHERE (upper(l.titulo) LIKE '%' || upper(:texto) || '%' 
                        OR l.quantidade = :texto 
                        OR l.id = :texto)
                       AND l.status != 'E'
                    ORDER BY l.id DESC""")
    fun getSearch(texto: String): List<Leitura>

    @Query("""SELECT * 
                      FROM leituras l
                     WHERE (upper(l.titulo) LIKE '%' || upper(:texto) || '%' 
                        OR l.quantidade = :texto 
                        OR l.id = :texto)
                       AND l.status = 'E'
                    ORDER BY l.id DESC""")
    fun getLixeiraSearch(texto: String): List<Leitura>

    @Query("SELECT * FROM leituras WHERE id = :id")
    fun getByID(id: Long): List<Leitura>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(leitura: Leitura): Long

    @Update
    fun update(leitura: Leitura)

    @Delete
    fun delete(leitura: Leitura)

    @Query("""
        DELETE FROM leituras 
         WHERE status = 'E'
        """)
    fun deleteLixeira()
}