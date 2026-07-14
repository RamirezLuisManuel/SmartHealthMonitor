package mx.utng.smarthealthmonitor.lmrr.tv

import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

/**
 * Repository del módulo TV.
 * Optimizado para ser reactivo al estado del DAO y evitar fallos de inicialización.
 */
object SmartHealthRepository {

    // FC actual en tiempo real
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    // Flujo para el DAO que permite reactividad si se inicializa tarde
    private val _daoFlow = MutableStateFlow<LecturaFCDao?>(null)

    /** Inicializar con el contexto de la aplicación */
    fun init(context: Context) {
        if (_daoFlow.value != null) return // Evitar re-inicialización innecesaria
        
        val database = SmartHealthTvDB.getDatabase(context)
        _daoFlow.value = database.lecturaDao()
    }

    /** Actualiza el FC actual y persiste la lectura en Room */
    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        _daoFlow.value?.insertar(LecturaFC(valorBpm = bpm))
    }

    /** Flow reactivo del historial de lecturas */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun obtenerHistorial(): Flow<List<LecturaFC>> =
        _daoFlow.flatMapLatest { dao ->
            dao?.obtenerUltimas() ?: emptyFlow()
        }
}
