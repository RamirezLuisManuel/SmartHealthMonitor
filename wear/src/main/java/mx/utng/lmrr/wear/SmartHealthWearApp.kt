package mx.utng.lmrr.wear

import android.app.Application
import mx.utng.lmrr.wear.data.SmartHealthRepository
import mx.utng.lmrr.wear.data.sync.WearSyncManager

class SmartHealthWearApp : Application() {
    
    companion object {
        lateinit var syncManager: WearSyncManager
    }

    override fun onCreate() {
        super.onCreate()
        SmartHealthRepository.init(this)
        
        syncManager = WearSyncManager(this)
    }
}
