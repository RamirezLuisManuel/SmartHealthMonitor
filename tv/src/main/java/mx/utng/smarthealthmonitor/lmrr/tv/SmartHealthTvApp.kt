package mx.utng.smarthealthmonitor.lmrr.tv

import android.app.Application
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmartHealthTvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar el Repositorio al arrancar la App
        SmartHealthRepository.init(this)
        
        // Poblar la base de datos con datos de prueba si está vacía
        MainScope().launch {
            try {
                val data = SmartHealthRepository.obtenerHistorial().first()
                if (data.isEmpty()) {
                    MockData.historialFC.forEach { lectura ->
                        SmartHealthRepository.actualizarFC(lectura.valorBpm)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
