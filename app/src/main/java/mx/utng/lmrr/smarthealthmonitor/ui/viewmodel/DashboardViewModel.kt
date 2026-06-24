package mx.utng.lmrr.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.lmrr.smarthealthmonitor.data.models.MockData
import mx.utng.lmrr.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.lmrr.smarthealthmonitor.data.db.LecturaFC

class DashboardViewModel : ViewModel() {

    // Observamos los flujos del repositorio real para que la UI reaccione
    // Si el valor es 0 (inicial), mostramos el valor de MockData para que no aparezca vacío
    val fc: StateFlow<Int> = SmartHealthRepository.fcFlow
        .map { if (it == 0) MockData.fcActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MockData.fcActual,
        )

    val pasos: StateFlow<Int> = SmartHealthRepository.pasosFlow
        .map { if (it == 0) MockData.pasosActual else it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MockData.pasosActual
        )

    // ← NUEVO: historial desde Room (Flow reactivo)
    val historial: StateFlow<List<LecturaFC>> =
        SmartHealthRepository.obtenerHistorial()
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
}
