package mx.utng.lmrr.wear.watchface

import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository

class SmartHealthWatchFaceService : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = SmartHealthRenderer(
            context                              = applicationContext,
            surfaceHolder                        = surfaceHolder,
            watchState                           = watchState,
            complicationSlotsManager             = complicationSlotsManager,
            currentUserStyleRepository           = currentUserStyleRepository,
            interactiveDrawModeUpdateDelayMillis = 1_000L
        )
        return WatchFace(WatchFaceType.DIGITAL, renderer)
    }
}
