package mx.utng.lmrr.wear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.utng.lmrr.wear.data.LecturaFCLocal
import mx.utng.lmrr.wear.data.SmartHealthRepository
import mx.utng.lmrr.wear.data.WearNeonRepository

class WearDashboardViewModel : ViewModel() {

    // ── Repositorio Neon (publica FC a PostgreSQL) ────────────────────────────
    private val neonRepo = WearNeonRepository()

    // ── FC en tiempo real desde el sensor (vía SmartHealthRepository) ─────────
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) 72 else it }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = 72
        )

    // ── Historial de lecturas locales ─────────────────────────────────────────
    val historial: StateFlow<List<LecturaFCLocal>> =
        SmartHealthRepository.historialFlow
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        // Observar cambios de FC y publicar a Neon en IO thread
        viewModelScope.launch {
            SmartHealthRepository.fcFlow.collect { bpm ->
                if (bpm > 0) {
                    val estado = when {
                        bpm < 60  -> "FC Baja"
                        bpm > 100 -> "FC Alta"
                        else      -> "Normal"
                    }
                    // Publicar a Neon en background (error silencioso si no hay red)
                    launch(Dispatchers.IO) {
                        runCatching { neonRepo.publicarLectura(bpm, estado) }
                            .onFailure { android.util.Log.w("WearVM", "Sin red: ${it.message}") }
                    }
                }
            }
        }
    }
}