package mx.utng.smarthealthmonitor.lmrr.tv

/**
 * Representa el estado de la UI para la pantalla de la TV.
 */
data class TvUiState(
    val fcActual   : Int = 0,
    val fcEstado   : String = "Desconectado",
    val ultimaHora : String = "--:--",
    val isLoading  : Boolean = true,
    val lecturas   : List<LecturaFC> = emptyList()
)
