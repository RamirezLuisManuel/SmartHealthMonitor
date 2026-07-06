package mx.utng.lmrr.wear.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SmartHealthRepository {
    private val _fcFlow = MutableStateFlow(72)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    // Historial en memoria (últimas 50 lecturas)
    private val _historialFlow = MutableStateFlow<List<LecturaFCLocal>>(emptyList())
    val historialFlow: StateFlow<List<LecturaFCLocal>> = _historialFlow.asStateFlow()

    private var nextId = 1

    fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm

        // Registrar en historial local
        val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val nuevaLectura = LecturaFCLocal(
            id       = nextId++,
            valorBpm = bpm,
            hora     = hora
        )
        val listaActual = _historialFlow.value.toMutableList()
        listaActual.add(0, nuevaLectura)               // más reciente primero
        if (listaActual.size > 50) listaActual.removeLast()
        _historialFlow.value = listaActual
    }
}