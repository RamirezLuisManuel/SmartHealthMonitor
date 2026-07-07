package mx.utng.smarthealthmonitor.lmrr.tv

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter

/**
 * MainFragment — UI principal de la app Android TV.
 *
 * Arquitectura Leanback:
 *   BrowseSupportFragment → sidebar de headers + área de contenido
 *   ListRow               → cada fila horizontal de cards
 *   FCCardPresenter       → convierte LecturaFC → ImageCardView
 *   ArrayObjectAdapter    → lista observable de ítems
 *
 * Esta arquitectura es idéntica a la de YouTube TV, Netflix y Cinépolis en Android TV.
 */
class MainFragment : BrowseSupportFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del BrowseFragment
        title        = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Color de la marca en el sidebar (barra lateral de headers)
        brandColor           = resources.getColor(R.color.sh_primary, null)
        searchAffordanceColor = resources.getColor(R.color.sh_amber, null)

        cargarFilas()
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // ── Fila 1: Estado actual (FC + Pasos) ───────────────────────
        val estadoAdapter = ArrayObjectAdapter(FCCardPresenter())
        // Datos simulados — en Ej.03 vendrán de Room via ViewModel
        estadoAdapter.add(LecturaFC(id = 0, valorBpm = 88,   hora = "Ahora"))
        estadoAdapter.add(LecturaFC(id = 1, valorBpm = 4250, hora = "Pasos"))
        rowsAdapter.add(ListRow(HeaderItem("Estado actual"), estadoAdapter))

        // ── Fila 2: Historial de FC ───────────────────────────────────
        val histAdapter = ArrayObjectAdapter(FCCardPresenter())
        MockData.historialFC.forEach { histAdapter.add(it) }
        rowsAdapter.add(ListRow(HeaderItem("Historial FC"), histAdapter))

        // Asignar el adapter al BrowseSupportFragment
        this.adapter = rowsAdapter
    }
}
