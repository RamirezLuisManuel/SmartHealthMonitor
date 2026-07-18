package mx.utng.lmrr.wear.data

/**
 * Versión local de LecturaFC para el módulo wear (sin Room).
 * Se mantiene en memoria en SmartHealthRepository.
 */
data class LecturaFCLocal(
    val id: Int,
    val valorBpm: Int,
    val hora: String,
    val esNormal: Boolean = valorBpm in 60..100,
    val sincronizado: Boolean = false
)
