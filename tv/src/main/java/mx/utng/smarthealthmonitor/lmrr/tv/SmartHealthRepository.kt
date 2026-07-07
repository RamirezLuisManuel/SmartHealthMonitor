package mx.utng.smarthealthmonitor.lmrr.tv

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Repository del módulo TV — patrón idéntico al de SmartHealthRepository del módulo app.
 * (El módulo :app es 'application', no 'library', por lo que no se puede depender de él.)
 *
 * En producción, este Repository se extraería a un módulo :data compartido (KMM o :core).
 */
object SmartHealthRepository {

    // FC actual en tiempo real (actualizado desde el sensor o Wearable Data Layer)
    private val _fcFlow = MutableStateFlow(0)
    val fcFlow: StateFlow<Int> = _fcFlow.asStateFlow()

    private var dao: LecturaFCDao? = null

    /** Inicializar con el contexto de la aplicación (llamar desde Application o MainActivity) */
    fun init(context: Context) {
        dao = SmartHealthTvDB.getDatabase(context).lecturaDao()
    }

    /** Actualiza el FC actual y persiste la lectura en Room */
    suspend fun actualizarFC(bpm: Int) {
        _fcFlow.value = bpm
        dao?.insertar(LecturaFC(valorBpm = bpm))
    }

    /** Flow reactivo del historial de lecturas — Room emite automáticamente al cambiar */
    fun obtenerHistorial(): Flow<List<LecturaFC>> =
        dao?.obtenerUltimas() ?: emptyFlow()
}
