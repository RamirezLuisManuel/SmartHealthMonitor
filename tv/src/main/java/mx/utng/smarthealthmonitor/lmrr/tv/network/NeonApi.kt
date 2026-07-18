package mx.utng.smarthealthmonitor.lmrr.tv.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// ════════════════════════════════════════════════════════════════════════════
// LEGACY REST API  (mantiene compatibilidad con SmartHealthRepository)
// ════════════════════════════════════════════════════════════════════════════

@Serializable
data class LecturaNeon(
    val id          : Int?   = null,
    val bpm         : Int,
    val estado      : String,
    val dispositivo : String,
    val hora        : String,
    val fecha       : String? = null
)

interface NeonService {
    @GET("api/lecturas")
    suspend fun getLecturas(): Response<List<LecturaNeon>>
}

object NeonApi {
    // URL placeholder — reemplazar con la URL real cuando se use la Legacy API
    private const val BASE_URL = "https://tu-api-neon.vercel.app/"
    private val json = Json { ignoreUnknownKeys = true }

    val service: NeonService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NeonService::class.java)
    }
}

// ════════════════════════════════════════════════════════════════════════════
// NEON HTTP SQL API  (nueva implementación para TvNeonRepository)
// ════════════════════════════════════════════════════════════════════════════

/** Cuerpo de la Neon HTTP API — ejecuta SQL parametrizado */
@Serializable
data class NeonRequest(
    val query  : String,
    val params : JsonArray = JsonArray(emptyList())
)

/** DTO que mapea una fila de lecturas_fc en PostgreSQL */
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
data class NeonSqlResponse(
    val command  : String             = "",
    @SerialName("rowCount") val rowCount: Int = 0,
    val rows     : List<LecturaFcDto> = emptyList()
)

/** Interfaz Retrofit para la Neon SQL HTTP API */
interface NeonSqlService {
    @POST("sql")
    suspend fun executeQuery(
        @Header("Authorization")          auth    : String,
        @Header("Neon-Connection-String") connStr : String,
        @Body                             request : NeonRequest
    ): NeonSqlResponse
}

/**
 * NeonClient — Singleton Retrofit para la Neon SQL HTTP API (módulo TV).
 * La TV usa este cliente para leer el historial completo de los 3 dispositivos.
 */
object NeonClient {
    private val BASE_URL = "https://${mx.utng.smarthealthmonitor.lmrr.tv.BuildConfig.NEON_HOST}/"

    val AUTH_HEADER = "Bearer ${mx.utng.smarthealthmonitor.lmrr.tv.BuildConfig.NEON_API_KEY}"
    val CONN_STRING = mx.utng.smarthealthmonitor.lmrr.tv.BuildConfig.NEON_CONN_STRING

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues  = true
    }

    val api: NeonSqlService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                    .build()
            )
            .build()
            .create(NeonSqlService::class.java)
    }
}