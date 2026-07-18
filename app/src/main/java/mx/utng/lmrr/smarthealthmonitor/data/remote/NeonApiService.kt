package mx.utng.lmrr.smarthealthmonitor.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// ── DTOs ─────────────────────────────────────────────────────────────────────

/** Cuerpo de la Neon HTTP API — ejecuta SQL parametrizado */
@Serializable
data class NeonRequest(
    val query: String,
    val params: JsonArray = JsonArray(emptyList())
)

/** DTO que mapea una fila de la tabla lecturas_fc en PostgreSQL */
@Serializable
data class LecturaFcDto(
    val id          : Int    = 0,
    val bpm         : Int    = 0,
    val estado      : String = "",
    val dispositivo : String = "",
    val hora        : String = "",
    val fecha       : String = "",
    @SerialName("created_at") val createdAt: String = ""
)

/** Respuesta estándar de la Neon HTTP API */
@Serializable
data class NeonResponse(
    val command  : String           = "",
    @SerialName("rowCount") val rowCount: Int = 0,
    val rows     : List<LecturaFcDto> = emptyList()
)

// ── Interface Retrofit ────────────────────────────────────────────────────────

/**
 * NeonApiService — ejecuta consultas SQL directamente contra Neon vía HTTP.
 * Endpoint: POST https://{NEON_HOST}/sql
 * Auth:     Authorization: Bearer {NEON_API_KEY}
 */
interface NeonApiService {

    @POST("sql")
    suspend fun executeQuery(
        @Header("Authorization")          auth    : String,
        @Header("Neon-Connection-String") connStr : String,
        @Body                             request : NeonRequest
    ): NeonResponse
}
