package mx.utng.lmrr.smarthealthmonitor.data

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearListenerService : WearableListenerService() {

    // 1. Creamos un Scope para ejecutar tareas en segundo plano (como guardar en Room)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        const val PATH_FC    = "/smarthealthmonitor/fc"
        const val PATH_PASOS = "/smarthealthmonitor/pasos"
        private const val TAG = "WearListener"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "onMessageReceived - INICIO")
        val data   = String(messageEvent.data)
        val path   = messageEvent.path
        Log.d(TAG, "Mensaje recibido: path=$path, data=$data")

        when (path) {
            PATH_FC -> {
                val bpm = data.toIntOrNull() ?: return
                // 2. Usamos scope.launch para poder llamar a la función suspend
                scope.launch {
                    SmartHealthRepository.actualizarFC(bpm)
                    Log.d(TAG, "FC guardada en Room exitosamente")
                }
            }
            PATH_PASOS -> {
                val pasos = data.toIntOrNull() ?: return
                // actualizarPasos no es suspend, así que se queda normal
                SmartHealthRepository.actualizarPasos(pasos)
            }
            else -> Log.w(TAG, "Path desconocido: $path")
        }
    }

    // 3. Es buena práctica cancelar las tareas pendientes cuando se cierra el servicio
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}