package mx.utng.lmrr.smarthealthmonitor

import android.app.Application
import mx.utng.lmrr.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.lmrr.smarthealthmonitor.data.sync.NeonSyncWorker

/**
 * SmartHealthApp — Application class.
 *
 * Al iniciar:
 *  1. Inicializa Room + MQTT (SmartHealthRepository).
 *  2. Programa el sync periódico Room ↔ Neon vía WorkManager (30 min).
 */
class SmartHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)

        // Programar sincronización periódica con Neon PostgreSQL
        NeonSyncWorker.schedule(this)
    }
}