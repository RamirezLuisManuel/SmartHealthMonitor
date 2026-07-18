package mx.utng.lmrr.smarthealthmonitor.data.db

import androidx.room.*

@Entity(tableName = "lecturas_fc")
data class LecturaFC(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val valorBpm: Int,
    val estado: String = "Normal",
    val dispositivo: String = "app", // wear | app | tv
    val timestamp: Long = System.currentTimeMillis(),
    val hora: String = java.text.SimpleDateFormat(
        "HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date()),
    val fecha: String = java.text.SimpleDateFormat(
        "yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date()),
    val esNormal: Boolean = valorBpm in 60..100,
    val sincronizado: Boolean = false
)