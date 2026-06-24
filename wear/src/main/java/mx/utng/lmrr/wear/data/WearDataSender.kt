package mx.utng.lmrr.wear.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearDataSender(private val context: Context) {

    suspend fun enviarFC(bpm: Int) {
        Log.d("WearDataSender", "Intentando enviar FC: $bpm")
        enviarMensaje("/smarthealthmonitor/fc", bpm.toString())
    }

    suspend fun enviarPasos(pasos: Int) {
        Log.d("WearDataSender", "Intentando enviar Pasos: $pasos")
        enviarMensaje("/smarthealthmonitor/pasos", pasos.toString())
    }

    private suspend fun enviarMensaje(path: String, data: String) {
        try {
            // Busca nodos (teléfonos) conectados
            val allNodes = Wearable.getNodeClient(context).connectedNodes.await()

            if (allNodes.isEmpty()) {
                Log.w("WearDataSender", "FALLO: No se detectaron dispositivos conectados.")
                return
            }

            allNodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, path, data.toByteArray())
                    .await()
                Log.d("WearDataSender", "Mensaje enviado a ${node.displayName}: path=$path data=$data")
            }
        } catch (e: Exception) {
            Log.e("WearDataSender", "Error al enviar mensaje", e)
        }
    }
}