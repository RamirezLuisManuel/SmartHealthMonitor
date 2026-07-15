package mx.utng.smarthealthmonitor.lmrr.tv

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.lmrr.tv.mqtt.MqttTvSubscriber
import mx.utng.lmrr.smarthealthmonitor.mqtt.TvMessage

/**
 * TvViewModel actualizado para observar mensajes MQTT y el historial de Room.
 */
class TvViewModel(
    private val repository: SmartHealthRepository = SmartHealthRepository,
    context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    // Flow de mensajes MQTT entrantes
    private val mqttFlow = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(context, mqttFlow)

    init {
        mqttSubscriber.connect()

        // Observar mensajes MQTT y actualizar el estado de la UI
        viewModelScope.launch {
            mqttFlow.collect { tvMsg ->
                tvMsg ?: return@collect
                _state.update { it.copy(
                    fcActual   = tvMsg.bpm,
                    fcEstado   = tvMsg.estado,
                    ultimaHora = tvMsg.hora,
                    isLoading  = false
                )}
            }
        }

        // Observar historial de Room
        viewModelScope.launch {
            repository.obtenerHistorial().collect { lista ->
                _state.update { it.copy(lecturas = lista) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
    }
}
