package mx.utng.lmrr.wear.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.utng.lmrr.wear.data.SmartHealthRepository
import mx.utng.lmrr.wear.data.network.LecturaNeon
import mx.utng.lmrr.wear.data.network.NeonApi

class WearSyncManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun attemptSync() {
        if (!isWifiConnected()) {
            Log.d("WearSyncManager", "WiFi no conectado, posponiendo sincronización.")
            return
        }

        scope.launch {
            Log.d("WearSyncManager", "Iniciando sincronización desde Wear...")
            val historial = SmartHealthRepository.getHistorialNoSincronizado()
            
            if (historial.isEmpty()) {
                Log.d("WearSyncManager", "No hay datos pendientes en Wear.")
                return@launch
            }

            historial.forEach { lectura ->
                try {
                    val lecturaNeon = LecturaNeon(
                        bpm = lectura.valorBpm,
                        estado = "Normal",
                        dispositivo = "wear",
                        hora = lectura.hora
                    )
                    
                    val response = NeonApi.service.postLectura(lecturaNeon)
                    if (response.isSuccessful) {
                        SmartHealthRepository.marcarSincronizado(lectura.id)
                        Log.d("WearSyncManager", "Lectura Wear ${lectura.id} sincronizada.")
                    }
                } catch (e: Exception) {
                    Log.e("WearSyncManager", "Error sincronizando lectura Wear ${lectura.id}", e)
                }
            }
        }
    }

    private fun isWifiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}