package mx.utng.smarthealthmonitor.lmrr.tv

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.*
import mx.utng.smarthealthmonitor.lmrr.tv.network.NeonApi
import mx.utng.smarthealthmonitor.lmrr.tv.network.LecturaNeon

/**
 * Repository del módulo TV.
 * Ahora basado en la API de Neon para lectura en tiempo real.
 */
object SmartHealthRepository {

    // FC actual en tiempo real (seguimos recibiendo por MQTT vía ViewModel)
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    /** Inicializar con el contexto de la aplicación */
    fun init(context: Context) {
        // No Room initialization needed anymore
    }

    /** Actualiza el FC actual (recibido por MQTT) */
    fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
    }

    /** Flow reactivo del historial de lecturas desde Neon API */
    fun obtenerHistorial(): Flow<List<LecturaFC>> = flow {
        while (true) {
            try {
                val response = NeonApi.service.getLecturas()
                if (response.isSuccessful) {
                    val lecturas = response.body()?.map { it.toLocal() } ?: emptyList()
                    emit(lecturas)
                }
            } catch (e: Exception) {
                Log.e("SmartHealthRepository", "Error al obtener historial de Neon", e)
            }
            kotlinx.coroutines.delay(10000) // Refrescar cada 10 segundos
        }
    }

    private fun LecturaNeon.toLocal(): LecturaFC {
        return LecturaFC(
            id = this.id ?: 0,
            valorBpm = this.bpm,
            hora = this.hora,
            esNormal = this.bpm in 60..100
        )
    }
}