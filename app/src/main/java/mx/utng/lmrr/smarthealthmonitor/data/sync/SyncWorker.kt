package mx.utng.lmrr.smarthealthmonitor.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import mx.utng.lmrr.smarthealthmonitor.data.db.SmartHealthDB
import mx.utng.lmrr.smarthealthmonitor.data.repository.SyncRepository
import java.util.concurrent.TimeUnit

private const val TAG = "NeonSyncWorker"

/**
 * NeonSyncWorker — sincroniza Room ↔ Neon en background cada 30 minutos.
 *
 * 1. Envía a Neon las lecturas locales pendientes (sincronizado = false).
 * 2. Descarga de Neon las lecturas más recientes y actualiza Room (upsert).
 *
 * WorkManager reintenta automáticamente con backoff exponencial si no hay red.
 */
class NeonSyncWorker(
    ctx    : Context,
    params : WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "🔄 Iniciando sync Room ↔ Neon...")
        return try {
            val db   = SmartHealthDB.getDatabase(applicationContext)
            val repo = SyncRepository(db.lecturaDao())

            // 1. Enviar lecturas locales pendientes a Neon
            repo.enviarPendientes()

            // 2. Descargar los más recientes de Neon → Room
            repo.sincronizarDesdeNeon(limite = 100)

            Log.d(TAG, "✅ Sync completado")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync fallido: ${e.message}")
            Result.retry()   // WorkManager reintentará automáticamente
        }
    }

    companion object {
        const val WORK_NAME = "NeonSyncWork"

        /**
         * Programa la sincronización periódica cada 30 minutos.
         * Solo se ejecuta cuando hay red disponible.
         * Se llama desde SmartHealthApp.onCreate().
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<NeonSyncWorker>(
                30, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "⏱️ Sync periódico programado cada 30 min")
        }
    }
}