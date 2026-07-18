package mx.utng.lmrr.smarthealthmonitor.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaFCDao {

    // ── Consultas existentes ──────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lectura: LecturaFC): Long

    @Query("""
       SELECT * FROM lecturas_fc
       ORDER BY timestamp DESC
       LIMIT 50""")
    fun obtenerUltimas(): Flow<List<LecturaFC>>

    /** Alias reactivo de obtenerUltimas() — usado por SyncRepository */
    @Query("SELECT * FROM lecturas_fc ORDER BY timestamp DESC")
    fun obtenerTodas(): Flow<List<LecturaFC>>

    @Query("SELECT COUNT(*) FROM lecturas_fc")
    suspend fun contarRegistros(): Int

    @Query("""
        DELETE FROM lecturas_fc
        WHERE timestamp < :limite""")
    suspend fun limpiarViejos(limite: Long): Int

    // ── Métodos de sincronización con Neon ───────────────────────────────────

    /** Obtener lecturas que aún NO se enviaron a Neon */
    @Query("SELECT * FROM lecturas_fc WHERE sincronizado = 0")
    suspend fun obtenerNoSincronizados(): List<LecturaFC>

    /** Marcar un registro como sincronizado con Neon (acepta Int) */
    @Query("UPDATE lecturas_fc SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizado(id: Int)

    /** Marcar un registro como sincronizado con Neon (acepta Long — retorno de insertar) */
    @Query("UPDATE lecturas_fc SET sincronizado = 1 WHERE id = :id")
    suspend fun marcarSincronizadoLong(id: Long)

    /** Upsert: inserta o reemplaza si el id ya existe (para sincronización Neon → Room) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lectura: LecturaFC)

    /** Flow reactivo con el conteo de registros pendientes de sync */
    @Query("SELECT COUNT(*) FROM lecturas_fc WHERE sincronizado = 0")
    fun contarPendientes(): Flow<Int>
}