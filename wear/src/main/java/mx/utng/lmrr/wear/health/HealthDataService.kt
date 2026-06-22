package mx.utng.lmrr.wear.health

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.data.SampleDataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking
import mx.utng.lmrr.wear.data.WearDataSender

class HealthDataService : PassiveListenerService() {

    private lateinit var wearDataSender: WearDataSender

    override fun onCreate() {
        super.onCreate()
        wearDataSender = WearDataSender(applicationContext)
    }

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val fcDataPoints = dataPoints.getData(DataType.HEART_RATE_BPM)
        val ultimoDatoFC = fcDataPoints.lastOrNull() as? SampleDataPoint<Double>

        if (ultimoDatoFC != null) {
            val bpm = ultimoDatoFC.value.toInt()
            Log.d("HealthDataService", "FC recibida desde sensor virtual: $bpm")

            runBlocking(Dispatchers.IO) {
                wearDataSender.enviarFC(bpm)
            }
        }
    }

    companion object {
        suspend fun registrar(context: Context) {
            val hsClient = HealthServices.getClient(context)
            val passiveClient = hsClient.passiveMonitoringClient

            val config = PassiveListenerConfig.builder()
                .setDataTypes(setOf(DataType.HEART_RATE_BPM))
                .setShouldUserActivityInfoBeRequested(true)
                .build()

            try {
                passiveClient.setPassiveListenerServiceAsync(
                    HealthDataService::class.java,
                    config
                ).await()
                Log.d("HealthDataService", "Servicio registrado correctamente")
            } catch (e: Exception) {
                Log.e("HealthDataService", "Error al registrar Health Services", e)
            }
        }
    }
}
