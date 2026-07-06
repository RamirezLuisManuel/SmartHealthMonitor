package mx.utng.lmrr.wear.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import mx.utng.lmrr.wear.data.LecturaFCLocal
import mx.utng.lmrr.wear.data.SmartHealthRepository

class WearDashboardViewModel : ViewModel() {

    // FC en tiempo real desde el Repository
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) 72 else it }  // valor por defecto
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 72
        )

    // Historial de lecturas desde Room (wear usa lista en memoria)
    val historial: StateFlow<List<LecturaFCLocal>> =
        SmartHealthRepository.historialFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}