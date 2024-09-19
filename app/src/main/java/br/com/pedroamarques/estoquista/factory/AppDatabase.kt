package br.com.pedroamarques.estoquista.factory

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.pedroamarques.estoquista.dao.*
import br.com.pedroamarques.estoquista.entities.*
import br.com.pedroamarques.estoquista.helper.Converters
import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.util.Calendar

@Database(entities = arrayOf(Leitura::class,
                             ItemLeitura::class,
                             Lote::class),
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun leituraDao(): LeituraDao
    abstract fun itemLeituraDao(): ItemLeituraDao
    abstract fun loteDao(): LoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context):  AppDatabase {
            val tempInstance = INSTANCE

            if(tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {

                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "estoquista")
                    .fallbackToDestructiveMigration()
                    //.addMigrations()
                    .build()

                INSTANCE = instance
                return instance
            }
        }

        fun recreate(context: Context) {
            this.getDatabase(context).close()
            val dbFile = context.getDatabasePath("estoquista")
            if (dbFile.exists()) {
                dbFile.delete()
            }
            this.getDatabase(context)
        }

        fun closeIfNeeded() {
            if(INSTANCE?.isOpen == true) {
                if(INSTANCE?.inTransaction() == true) {
                    try {
                        INSTANCE?.setTransactionSuccessful()
                        INSTANCE?.endTransaction()
                    } catch (ex: Exception) {
                        Timber.e("Não foi possível finalizar transação: %s", ex.localizedMessage)
                    }
                }
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}