package mx.utng.lmrr.smarthealthmonitor.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import mx.utng.lmrr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.lmrr.smarthealthmonitor.data.db.LecturaFCDao
import mx.utng.lmrr.smarthealthmonitor.data.remote.NeonClient
import mx.utng.lmrr.smarthealthmonitor.data.remote.NeonRequest

private const val TAG = "SyncRepository"

/**
 * SyncRepository — repositorio híbrido Room + Neon.
 *
 * Patrón offline-first:
 *   1. Room es la fuente de verdad LOCAL → siempre disponible sin internet.
 *   2. Neon es la fuente de verdad en la NUBE → sincronización bidireccional.
 *
 * Room → Neon : al insertar cada lectura se intenta enviarla.
 * Neon → Room : sincronización periódica vía NeonSyncWorker (30 min).
 */
class SyncRepository(private val dao: LecturaFCDao) {

    // ── LECTURA LOCAL (offline-first) ─────────────────────────────────────────

    /** Flow reactivo de Room — siempre disponible aunque no haya internet. */
    fun observarHistorial(): Flow<List<LecturaFC>> = dao.obtenerTodas()

    /** Flow del conteo de registros pendientes de sync con Neon. */
    fun contarPendientes(): Flow<Int> = dao.contarPendientes()

    // ── ESCRITURA LOCAL + SYNC ────────────────────────────────────────────────

    /**
     * Guarda en Room primero (garantiza persistencia local),
     * luego intenta sincronizar con Neon en background.
     * Si no hay internet, queda pendiente para el próximo sync.
     */
    suspend fun insertarLectura(lectura: LecturaFC) {
        // 1. Guardar localmente PRIMERO (nunca falla)
        val id = dao.insertar(lectura)

        // 2. Intentar sync con Neon (puede fallar sin internet)
        try {
            sincronizarHaciaNeon(lectura)
            dao.marcarSincronizadoLong(id)
            Log.d(TAG, "✅ Lectura $id enviada a Neon")
        } catch (e: Exception) {
            Log.w(TAG, "⏳ Sin red — lectura $id pendiente de sync: ${e.message}")
        }
    }

    // ── PUSH: Room → Neon ─────────────────────────────────────────────────────

    private suspend fun sincronizarHaciaNeon(lectura: LecturaFC) =
        withContext(Dispatchers.IO) {
            NeonClient.api.executeQuery(
                auth    = NeonClient.AUTH_HEADER,
                connStr = NeonClient.CONN_STRING,
                request = NeonRequest(
                    query  = """INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora)
                               VALUES ($1, $2, $3, $4) RETURNING id""".trimIndent(),
                    params = JsonArray(listOf(
                        JsonPrimitive(lectura.valorBpm),
                        JsonPrimitive(lectura.estado),
                        JsonPrimitive(lectura.dispositivo),
                        JsonPrimitive(lectura.hora)
                    ))
                )
            )
        }

    // ── PULL: Neon → Room ─────────────────────────────────────────────────────

    /**
     * Descarga los registros más recientes de Neon y actualiza Room (upsert).
     * Se llama desde NeonSyncWorker en background cada 30 min.
     */
    suspend fun sincronizarDesdeNeon(limite: Int = 50) = withContext(Dispatchers.IO) {
        val response = NeonClient.api.executeQuery(
            auth    = NeonClient.AUTH_HEADER,
            connStr = NeonClient.CONN_STRING,
            request = NeonRequest(
                query  = """SELECT id, bpm, estado, dispositivo, hora
                           FROM lecturas_fc
                           ORDER BY created_at DESC
                           LIMIT $1""".trimIndent(),
                params = JsonArray(listOf(JsonPrimitive(limite)))
            )
        )

        response.rows.forEach { dto ->
            dao.upsert(
                LecturaFC(
                    id           = dto.id,
                    valorBpm     = dto.bpm,
                    estado       = dto.estado,
                    dispositivo  = dto.dispositivo,
                    hora         = dto.hora,
                    sincronizado = true
                )
            )
        }
        Log.d(TAG, "✅ ${response.rowCount} registros descargados de Neon")
    }

    // ── Enviar pendientes ─────────────────────────────────────────────────────

    /**
     * Intenta enviar a Neon todas las lecturas locales con sincronizado = false.
     * Se llama desde NeonSyncWorker cuando se recupera la conexión.
     */
    suspend fun enviarPendientes() = withContext(Dispatchers.IO) {
        val pendientes = dao.obtenerNoSincronizados()
        Log.d(TAG, "📤 Enviando ${pendientes.size} pendientes a Neon...")
        pendientes.forEach { lectura ->
            try {
                sincronizarHaciaNeon(lectura)
                dao.marcarSincronizado(lectura.id)
                Log.d(TAG, "✅ Pendiente sincronizado: id=${lectura.id}")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Aún sin internet para id=${lectura.id}: ${e.message}")
            }
        }
    }
}
