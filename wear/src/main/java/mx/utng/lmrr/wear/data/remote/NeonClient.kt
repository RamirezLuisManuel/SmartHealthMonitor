package mx.utng.lmrr.wear.data.remote

import kotlinx.serialization.json.Json
import mx.utng.lmrr.wear.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * NeonClient — Singleton Retrofit para la Neon HTTP API (módulo Wear OS).
 *
 * El reloj usa este cliente para publicar lecturas de FC directamente a Neon
 * sin necesidad de un Room local (ahorro de memoria en el reloj).
 */
object NeonClient {

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
