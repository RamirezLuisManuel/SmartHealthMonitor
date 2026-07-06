package mx.utng.lmrr.wear.watchface

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.SurfaceHolder
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.DrawMode
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import mx.utng.lmrr.wear.data.SmartHealthRepository
import java.time.ZonedDateTime

class SmartHealthRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    complicationSlotsManager: ComplicationSlotsManager,
    currentUserStyleRepository: CurrentUserStyleRepository,
    interactiveDrawModeUpdateDelayMillis: Long
) : Renderer.CanvasRenderer2<Renderer.SharedAssets>(
    surfaceHolder,
    currentUserStyleRepository,
    watchState,
    androidx.wear.watchface.CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis
) {

    private val paintHora = Paint().apply {
        color       = Color.WHITE
        textSize    = 72f
        isAntiAlias = true
        typeface    = Typeface.DEFAULT_BOLD
    }

    private val paintFC = Paint().apply {
        color       = Color.RED
        textSize    = 30f
        isAntiAlias = true
    }

    private val paintSub = Paint().apply {
        color       = Color.GRAY
        textSize    = 22f
        isAntiAlias = true
    }

    // Pinturas modo AOD (Always-On Display) — sin antiAlias para ahorrar batería
    private val paintHoraAod = Paint().apply {
        color       = Color.WHITE
        textSize    = 72f
        isAntiAlias = false
        typeface    = Typeface.DEFAULT_BOLD
    }

    override suspend fun createSharedAssets(): SharedAssets =
        object : SharedAssets { override fun onDestroy() {} }

    override fun render(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT

        // Fondo negro — ahorra batería en modo AOD
        canvas.drawColor(Color.BLACK)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()

        if (isAmbient) {
            // Modo AOD: solo la hora en blanco sobre negro puro
            val hora = String.format("%02d:%02d", zonedDateTime.hour, zonedDateTime.minute)
            val tw = paintHoraAod.measureText(hora)
            canvas.drawText(hora, cx - tw / 2, cy + 26f, paintHoraAod)
        } else {
            // Modo interactivo: hora + segundos + FC
            val hora = String.format("%02d:%02d", zonedDateTime.hour, zonedDateTime.minute)
            val tw = paintHora.measureText(hora)
            canvas.drawText(hora, cx - tw / 2, cy - 10f, paintHora)

            // Segundos (pequeño debajo de la hora)
            val seg = String.format("%02d", zonedDateTime.second)
            canvas.drawText(seg, cx - 18f, cy + 30f, paintSub)

            // FC desde SmartHealthRepository
            val fc = SmartHealthRepository.fcFlow.value
            if (fc > 0) {
                val fcStr = "❤ $fc bpm"
                val fcW = paintFC.measureText(fcStr)
                canvas.drawText(fcStr, cx - fcW / 2, cy + 70f, paintFC)
            }
        }
    }

    override fun renderHighlightLayer(
        canvas: Canvas,
        bounds: Rect,
        zonedDateTime: ZonedDateTime,
        sharedAssets: SharedAssets
    ) {
        canvas.drawColor(renderParameters.highlightLayer!!.backgroundTint)
    }
}
