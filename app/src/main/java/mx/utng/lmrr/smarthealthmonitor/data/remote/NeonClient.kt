package mx.utng.lmrr.smarthealthmonitor.data.remote

import kotlinx.serialization.json.Json
import mx.utng.lmrr.smarthealthmonitor.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * NeonClient — Singleton Retrofit para la Neon HTTP API.
 *
 * Uso:
 *   NeonClient.api.executeQuery(NeonClient.AUTH_HEADER, NeonClient.CONN_STRING, request)
 */
object NeonClient {

    // Base URL: https://{NEON_HOST}/
    private val BASE_URL = "https://${BuildConfig.NEON_HOST}/"

    /** Header de autorización con la API Key de Neon */
    val AUTH_HEADER = "Bearer ${BuildConfig.NEON_API_KEY}"

    /** Connection string completa para el header Neon-Connection-String */
    val CONN_STRING = BuildConfig.NEON_CONN_STRING

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues  = true
    }

    val api: NeonApiService by lazy {
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
            .create(NeonApiService::class.java)
    }
}
