package mx.utng.smarthealthmonitor.lmrr.tv

/**
 * Modelo local de lectura de frecuencia cardiaca para el módulo TV.
 * No depende de Room — los datos vienen de MockData o del Repository compartido.
 */
data class LecturaFC(
    val id: Int,
    val valorBpm: Int,
    val hora: String,
    val esNormal: Boolean = valorBpm in 60..100
)
