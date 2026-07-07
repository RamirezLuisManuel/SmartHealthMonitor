package mx.utng.smarthealthmonitor.lmrr.tv

import android.graphics.Color
import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter

/**
 * FCCardPresenter — convierte cada LecturaFC en una ImageCardView de Leanback.
 *
 * Arquitectura Presenter de Leanback:
 *   onCreateViewHolder  → infla / construye la vista
 *   onBindViewHolder    → vincula el dato a la vista
 *   onUnbindViewHolder  → limpia la vista para evitar leaks
 *
 * CRÍTICO: isFocusable + isFocusableInTouchMode son obligatorios para
 * que el D-pad pueda navegar a esta card.
 */
class FCCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            // Sin estas dos líneas el D-pad no puede navegar a este card
            isFocusable            = true
            isFocusableInTouchMode = true
            setMainImageDimensions(240, 180)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val card    = viewHolder.view as ImageCardView
        val lectura = item as? LecturaFC ?: return

        card.titleText   = "${lectura.valorBpm} bpm"
        card.contentText = lectura.hora

        // Color de fondo según si FC está en rango normal (60–100)
        val bgColor = if (lectura.esNormal) {
            Color.parseColor("#1B4F8A")  // azul primario — FC normal
        } else {
            Color.parseColor("#B3261E")  // rojo error — FC anormal
        }
        card.setBackgroundColor(bgColor)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // Liberar imagen para evitar memory leaks
        (viewHolder.view as ImageCardView).mainImage = null
    }
}
