package mx.utng.lmrr.smarthealthmonitor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import mx.utng.lmrr.smarthealthmonitor.data.models.MockData
// TODO: Importar repositorio cuando esté listo o usar uno simulado
// import mx.utng.lmrr.smarthealthmonitor.data.SmartHealthRepository

class DashboardViewModel : ViewModel() {

    // Simulación de flujos de datos mientras se implementa el repositorio real
    private val _fcFlow = MutableStateFlow(MockData.fcActual)
    val fc: StateFlow<Int> = _fcFlow.asStateFlow()

    private val _pasosFlow = MutableStateFlow(MockData.pasosActual)
    val pasos: StateFlow<Int> = _pasosFlow.asStateFlow()

    val historial = MockData.historialFC
}
