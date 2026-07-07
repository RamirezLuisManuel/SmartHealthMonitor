package mx.utng.smarthealthmonitor.lmrr.tv

/**
 * Datos simulados para el módulo TV.
 * Reemplazará con Room en el Ej.03.
 */
object MockData {
    val historialFC: List<LecturaFC> = listOf(
        LecturaFC(id = 1,  valorBpm = 72,  hora = "08:00"),
        LecturaFC(id = 2,  valorBpm = 85,  hora = "09:15"),
        LecturaFC(id = 3,  valorBpm = 110, hora = "10:30"),   // anormal
        LecturaFC(id = 4,  valorBpm = 68,  hora = "11:45"),
        LecturaFC(id = 5,  valorBpm = 95,  hora = "12:00"),
        LecturaFC(id = 6,  valorBpm = 120, hora = "13:30"),   // anormal
        LecturaFC(id = 7,  valorBpm = 78,  hora = "14:00"),
        LecturaFC(id = 8,  valorBpm = 63,  hora = "15:15"),
        LecturaFC(id = 9,  valorBpm = 88,  hora = "16:30"),
        LecturaFC(id = 10, valorBpm = 74,  hora = "17:45")
    )
}
