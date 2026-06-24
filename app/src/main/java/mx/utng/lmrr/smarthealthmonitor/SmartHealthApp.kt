package mx.utng.lmrr.smarthealthmonitor

import android.app.Application
import mx.utng.lmrr.smarthealthmonitor.data.SmartHealthRepository

class SmartHealthApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this) // inicializar Room
    }
}