package mx.utng.lmrr.smarthealthmonitor.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import mx.utng.lmrr.smarthealthmonitor.data.SmartHealthRepository
import mx.utng.lmrr.smarthealthmonitor.data.db.SmartHealthDB
import mx.utng.lmrr.smarthealthmonitor.data.network.LecturaNeon
import mx.utng.lmrr.smarthealthmonitor.data.network.NeonApi

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Iniciando sincronización con Neon...")
        
        val database = SmartHealthDB.getDatabase(applicationContext)
        val dao = database.lecturaDao()
        
        val noSincronizados = dao.obtenerNoSincronizados()
        
        if (noSincronizados.isEmpty()) {
            Log.d("SyncWorker", "No hay lecturas pendientes de sincronizar.")
            return Result.success()
        }

        var errores = 0
        noSincronizados.forEach { lectura ->
            try {
                val lecturaNeon = LecturaNeon(
                    bpm = lectura.valorBpm,
                    estado = lectura.estado,
                    dispositivo = lectura.dispositivo,
                    hora = lectura.hora,
                    fecha = lectura.fecha
                )
                
                val response = NeonApi.service.postLectura(lecturaNeon)
                if (response.isSuccessful) {
                    dao.marcarSincronizado(lectura.id)
                    Log.d("SyncWorker", "Lectura ${lectura.id} sincronizada correctamente.")
                } else {
                    errores++
                    Log.e("SyncWorker", "Error al sincronizar lectura ${lectura.id}: ${response.code()}")
                }
            } catch (e: Exception) {
                errores++
                Log.e("SyncWorker", "Excepción al sincronizar lectura ${lectura.id}", e)
            }
        }

        return if (errores == 0) Result.success() else Result.retry()
    }
}