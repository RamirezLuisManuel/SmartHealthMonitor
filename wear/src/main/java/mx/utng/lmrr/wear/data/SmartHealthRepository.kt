package mx.utng.lmrr.wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import mx.utng.lmrr.wear.mqtt.MqttWearPublisher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context

object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(72)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    // Historial en memoria (últimas 50 lecturas)
    private val _historialFlow = MutableStateFlow<List<LecturaFCLocal>>(emptyList())
    val historialFlow: StateFlow<List<LecturaFCLocal>> = _historialFlow.asStateFlow()

    private var nextId = 1
    private var mqttPublisher: MqttWearPublisher? = null

    fun init(context: Context) {
        if (mqttPublisher == null) {
            mqttPublisher = MqttWearPublisher(context)
            mqttPublisher?.connect()
        }
    }

    fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        
        // Publicar por MQTT a HiveMQ Cloud
        mqttPublisher?.publishFC(bpm, "Normal")

        // Registrar en historial local
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val nuevaLectura = LecturaFCLocal(
            id       = nextId++,
            valorBpm = bpm,
            hora     = hora,
            sincronizado = false
        )
        val listaActual = _historialFlow.value.toMutableList()
        listaActual.add(0, nuevaLectura)               // más reciente primero
        if (listaActual.size > 50) listaActual.removeLast()
        _historialFlow.value = listaActual
    }

    fun getHistorialNoSincronizado(): List<LecturaFCLocal> {
        return _historialFlow.value.filter { !it.sincronizado }
    }

    fun marcarSincronizado(id: Int) {
        val listaActual = _historialFlow.value.toMutableList()
        val index = listaActual.indexOfFirst { it.id == id }
        if (index != -1) {
            listaActual[index] = listaActual[index].copy(sincronizado = true)
            _historialFlow.value = listaActual
        }
    }
}