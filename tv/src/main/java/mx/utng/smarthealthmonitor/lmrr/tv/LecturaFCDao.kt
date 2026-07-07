package mx.utng.smarthealthmonitor.lmrr.tv

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaFCDao {

    // Void return avoids the Continuation<Long> wildcard name clash in Room + Kotlin 2.x
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lectura: LecturaFC)

    @Query("""
        SELECT * FROM lecturas_fc_tv
        ORDER BY timestamp DESC
        LIMIT 50
    """)
    fun obtenerUltimas(): Flow<List<LecturaFC>>

    // Flow<Int> instead of suspend fun to avoid Continuation<Int> clash
    @Query("SELECT COUNT(*) FROM lecturas_fc_tv")
    fun contarRegistros(): Flow<Int>
}
