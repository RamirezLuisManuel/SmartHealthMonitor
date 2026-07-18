package mx.utng.smarthealthmonitor.lmrr.tv

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import mx.utng.smarthealthmonitor.lmrr.tv.network.LecturaFcDto
import mx.utng.smarthealthmonitor.lmrr.tv.network.NeonClient
import mx.utng.smarthealthmonitor.lmrr.tv.network.NeonRequest

private const val TAG = "TvNeonRepository"

/**
 * TvNeonRepository — solo lectura.
 *
 * La TV NO inserta datos — lee el historial completo de los 3 dispositivos
 * desde Neon PostgreSQL y lo muestra en TvCatalogScreen.
 */
class TvNeonRepository {

    /**
     * Obtiene el historial completo de los 3 dispositivos.
     * Ordenado por created_at DESC para mostrar los más recientes primero.
     */
    suspend fun obtenerHistorialCompleto(limite: Int = 50): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            try {
                NeonClient.api.executeQuery(
                    auth    = NeonClient.AUTH_HEADER,
                    connStr = NeonClient.CONN_STRING,
                    request = NeonRequest(
                        query  = """SELECT id, bpm, estado, dispositivo, hora, created_at
                                   FROM lecturas_fc
                                   ORDER BY created_at DESC
                                   LIMIT $1""".trimIndent(),
                        params = JsonArray(listOf(JsonPrimitive(limite)))
                    )
                ).rows
                    .also { Log.d(TAG, "📺 ${it.size} lecturas descargadas de Neon") }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener historial de Neon: ${e.message}")
                emptyList()
            }
        }

    /**
     * Estadísticas de FC agrupadas por dispositivo.
     * Retorna el promedio de BPM y la última hora de cada dispositivo.
     */
    suspend fun obtenerEstadisticas(): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            try {
                NeonClient.api.executeQuery(
                    auth    = NeonClient.AUTH_HEADER,
                    connStr = NeonClient.CONN_STRING,
                    request = NeonRequest(
                        query = """SELECT
                                       dispositivo,
                                       ROUND(AVG(bpm))::int AS bpm,
                                       'Promedio'           AS estado,
                                       MAX(hora)            AS hora
                                   FROM lecturas_fc
                                   GROUP BY dispositivo
                                   ORDER BY dispositivo""".trimIndent()
                    )
                ).rows
                    .also { Log.d(TAG, "📊 ${it.size} estadísticas por dispositivo") }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener estadísticas de Neon: ${e.message}")
                emptyList()
            }
        }

    // ── Consultas avanzadas (Reto Extra) ──────────────────────────────────────

    /** Alertas: lecturas fuera de rango (< 60 o > 100 bpm) en las últimas 24 h */
    suspend fun obtenerAlertasRecientes(): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            try {
                NeonClient.api.executeQuery(
                    auth    = NeonClient.AUTH_HEADER,
                    connStr = NeonClient.CONN_STRING,
                    request = NeonRequest(
                        query = """SELECT id, bpm, estado, dispositivo, hora, created_at
                                   FROM lecturas_fc
                                   WHERE (bpm < 60 OR bpm > 100)
                                     AND created_at > NOW() - INTERVAL '24 hours'
                                   ORDER BY created_at DESC""".trimIndent()
                    )
                ).rows
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener alertas: ${e.message}")
                emptyList()
            }
        }

    /** Lectura más reciente de cada dispositivo (DISTINCT ON) */
    suspend fun obtenerUltimaPorDispositivo(): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            try {
                NeonClient.api.executeQuery(
                    auth    = NeonClient.AUTH_HEADER,
                    connStr = NeonClient.CONN_STRING,
                    request = NeonRequest(
                        query = """SELECT DISTINCT ON (dispositivo)
                                       dispositivo,
                                       bpm,
                                       estado,
                                       hora,
                                       created_at
                                   FROM lecturas_fc
                                   ORDER BY dispositivo, created_at DESC""".trimIndent()
                    )
                ).rows
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener últimas por dispositivo: ${e.message}")
                emptyList()
            }
        }
}

// ── Extension: DTO → Entity local ────────────────────────────────────────────

/** Convierte LecturaFcDto (Neon) a LecturaFC (Room/UI local del TV) */
fun LecturaFcDto.toLecturaFC() = LecturaFC(
    id       = this.id,
    valorBpm = this.bpm,
    hora     = this.hora.ifBlank { "--:--" },
    esNormal = this.bpm in 60..100
)
