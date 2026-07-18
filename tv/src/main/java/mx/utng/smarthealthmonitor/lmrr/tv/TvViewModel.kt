package mx.utng.smarthealthmonitor.lmrr.tv

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.utng.smarthealthmonitor.lmrr.tv.mqtt.MqttTvSubscriber
import mx.utng.lmrr.smarthealthmonitor.mqtt.TvMessage

/**
 * TvViewModel — actualizado para integrar TvNeonRepository.
 *
 * Fuentes de datos:
 *  1. MQTT   — FC en tiempo real (lectura del reloj).
 *  2. Room   — historial local del módulo TV.
 *  3. Neon   — historial completo + estadísticas de los 3 dispositivos.
 */
class TvViewModel(
    private val repository: SmartHealthRepository = SmartHealthRepository,
    context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(TvUiState())
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    // Exponer flujos específicos para composables como StateFlow
    val fc: StateFlow<Int> = state
        .map { it.fcActual }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val historial: StateFlow<List<LecturaFC>> = state
        .map { it.lecturas }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Repositorio Neon (solo lectura en TV) ─────────────────────────────────
    private val neonRepo = TvNeonRepository()

    // ── MQTT ──────────────────────────────────────────────────────────────────
    private val mqttFlow       = MutableStateFlow<TvMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(context, mqttFlow)

    init {
        repository.init(context)
        mqttSubscriber.connect()

        // Observar mensajes MQTT → FC en tiempo real
        viewModelScope.launch {
            mqttFlow.collect { tvMsg ->
                tvMsg ?: return@collect
                _state.update {
                    it.copy(
                        fcActual   = tvMsg.bpm,
                        fcEstado   = tvMsg.estado,
                        ultimaHora = tvMsg.hora,
                        isLoading  = false
                    )
                }
            }
        }

        // Observar historial de Room
        viewModelScope.launch {
            repository.obtenerHistorial().collect { lista ->
                _state.update { it.copy(lecturas = lista) }
            }
        }

        // Carga inicial desde Neon
        cargarDatos()
    }

    /**
     * Descarga historial completo + estadísticas desde Neon PostgreSQL.
     * Se llama automáticamente en init y manualmente con refresh().
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val lecturas     = neonRepo.obtenerHistorialCompleto(50)
                val estadisticas = neonRepo.obtenerEstadisticas()

                _state.update {
                    it.copy(
                        lecturas     = lecturas.map { dto -> dto.toLecturaFC() },
                        estadisticas = estadisticas.map { dto -> dto.toLecturaFC() },
                        isLoading    = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
                android.util.Log.e("TvViewModel", "Error cargando datos de Neon: ${e.message}")
            }
        }
    }

    /** Recarga el historial y estadísticas desde Neon (botón ↺ Actualizar). */
    fun refresh() = cargarDatos()

    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
    }
}
