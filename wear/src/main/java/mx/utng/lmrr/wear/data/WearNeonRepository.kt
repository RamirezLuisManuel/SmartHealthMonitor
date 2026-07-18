package mx.utng.lmrr.wear.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import mx.utng.lmrr.wear.data.remote.LecturaFcDto
import mx.utng.lmrr.wear.data.remote.NeonClient
import mx.utng.lmrr.wear.data.remote.NeonRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "WearNeonRepository"

/**
 * WearNeonRepository — publicación directa de FC a Neon desde el reloj.
 *
 * El Wear OS NO usa Room local (ahorro de memoria en el reloj).
 * Cada lectura del sensor se envía directamente a Neon vía HTTP API.
 * Si no hay WiFi, el error se captura silenciosamente.
 */
class WearNeonRepository {

    /**
     * Publica una lectura de FC a Neon PostgreSQL.
     * @param bpm   Frecuencia cardíaca en pulsaciones por minuto.
     * @param estado Estado: "FC Alta", "FC Baja" o "Normal".
     */
    suspend fun publicarLectura(bpm: Int, estado: String) =
        withContext(Dispatchers.IO) {
            val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            try {
                NeonClient.api.executeQuery(
                    auth    = NeonClient.AUTH_HEADER,
                    connStr = NeonClient.CONN_STRING,
                    request = NeonRequest(
                        query  = """INSERT INTO lecturas_fc (bpm, estado, dispositivo, hora)
                                   VALUES ($1, $2, $3, $4)""".trimIndent(),
                        params = JsonArray(listOf(
                            JsonPrimitive(bpm),
                            JsonPrimitive(estado),
                            JsonPrimitive("wear"),
                            JsonPrimitive(hora)
                        ))
                    )
                )
                Log.d(TAG, "⌚ FC enviada a Neon: $bpm bpm ($estado) @ $hora")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Sin red — FC no enviada a Neon: ${e.message}")
            }
        }

    /**
     * Obtiene las últimas 5 lecturas enviadas por el reloj desde Neon.
     * Útil para mostrar en el historial del Wear OS.
     */
    suspend fun obtenerUltimasLecturas(): List<LecturaFcDto> =
        withContext(Dispatchers.IO) {
            try {
                NeonClient.api.executeQuery(
                    auth    = NeonClient.AUTH_HEADER,
                    connStr = NeonClient.CONN_STRING,
                    request = NeonRequest(
                        query  = """SELECT id, bpm, estado, dispositivo, hora, created_at
                                   FROM lecturas_fc
                                   WHERE dispositivo = 'wear'
                                   ORDER BY created_at DESC
                                   LIMIT 5""".trimIndent()
                    )
                ).rows
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo historial Wear de Neon: ${e.message}")
                emptyList()
            }
        }
}
