package mx.utng.smarthealthmonitor.lmrr.tv

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * TvViewModel — expone los datos de SmartHealthRepository al MainFragment.
 *
 * Patrón idéntico al del módulo :app (ViewModel → Repository → Room → Flow).
 * La UI (MainFragment) solo observa StateFlows — nunca toca la BD directamente.
 */
class TvViewModel : ViewModel() {

    // FC actual del wearable (o 0 si no hay dato aún)
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .stateIn(
            scope   = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    // Historial de lecturas desde Room — se actualiza automáticamente con cada INSERT
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(
                scope   = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}
