package mx.utng.lmrr.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.lmrr.smarthealthmonitor.data.models.MockData
import mx.utng.lmrr.smarthealthmonitor.data.SmartHealthRepository

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

    val historial = MockData.historialFC
}
