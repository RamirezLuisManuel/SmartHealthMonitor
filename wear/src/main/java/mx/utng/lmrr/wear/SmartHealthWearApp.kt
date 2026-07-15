package mx.utng.lmrr.wear

import android.app.Application
import mx.utng.lmrr.wear.data.SmartHealthRepository

class SmartHealthWearApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)
    }
}
