package mx.utng.lmrr.smarthealthmonitor.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import mx.utng.lmrr.smarthealthmonitor.data.db.LecturaFC
import mx.utng.lmrr.smarthealthmonitor.data.db.LecturaFCDao
import mx.utng.lmrr.smarthealthmonitor.data.db.SmartHealthDB
import mx.utng.lmrr.smarthealthmonitor.mqtt.MqttAppService

object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private val _pasosFlow = MutableStateFlow(0)
    val pasosFlow: StateFlow<Int> = _pasosFlow.asStateFlow()

    private var dao: LecturaFCDao? = null
    private var mqttService: MqttAppService? = null

    fun init(context: Context) {
        dao = SmartHealthDB.getDatabase(context).lecturaDao()
        
        // Inicializar MQTT para recibir datos del reloj y re-publicar al TV
        if (mqttService == null) {
            mqttService = MqttAppService(context, _fcFlow)
            mqttService?.connect()
        }
    }

    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        // Persistir en Room automáticamente
        dao?.insertar(LecturaFC(valorBpm = bpm))
    }

    // Funciones adicionales de tu repositorio (como los pasos)
    fun actualizarPasos(pasos: Int) {
        // Implementar lógica de pasos si la necesitas
    }

    // Flow del historial desde Room
    fun obtenerHistorial(): Flow<List<LecturaFC>> =
        dao?.obtenerUltimas() ?: emptyFlow()
}