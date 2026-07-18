package mx.utng.smarthealthmonitor.lmrr.tv.network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

@Serializable
data class LecturaNeon(
    val id: Int? = null,
    val bpm: Int,
    val estado: String,
    val dispositivo: String,
    val hora: String,
    val fecha: String? = null
)

interface NeonService {
    @GET("api/lecturas")
    suspend fun getLecturas(): Response<List<LecturaNeon>>
}

object NeonApi {
    private const val BASE_URL = "https://tu-api-neon.vercel.app/" // Reemplazar con la URL real de Neon/Vercel

    private val json = Json { ignoreUnknownKeys = true }

    val service: NeonService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NeonService::class.java)
    }
}