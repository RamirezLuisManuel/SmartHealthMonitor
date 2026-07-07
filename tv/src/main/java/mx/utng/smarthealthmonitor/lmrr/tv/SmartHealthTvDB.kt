package mx.utng.smarthealthmonitor.lmrr.tv

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LecturaFC::class],
    version  = 1,
    exportSchema = false
)
abstract class SmartHealthTvDB : RoomDatabase() {

    abstract fun lecturaDao(): LecturaFCDao

    companion object {
        @Volatile
        private var INSTANCE: SmartHealthTvDB? = null

        fun getDatabase(context: Context): SmartHealthTvDB =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SmartHealthTvDB::class.java,
                    "smarthealthtv.db"
                ).build().also { INSTANCE = it }
            }
    }
}
