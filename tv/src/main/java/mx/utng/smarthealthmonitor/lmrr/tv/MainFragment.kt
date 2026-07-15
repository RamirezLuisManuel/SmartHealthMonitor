package mx.utng.smarthealthmonitor.lmrr.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import kotlinx.coroutines.launch

/**
 * MainFragment — UI principal de la app Android TV.
 *
 * Arquitectura Leanback + ViewModel:
 *   BrowseSupportFragment → sidebar de headers + área de contenido
 *   TvViewModel           → expone FC y historial como StateFlow
 *   ListRow               → cada fila horizontal de cards
 *   FCCardPresenter       → convierte LecturaFC → ImageCardView
 *   ArrayObjectAdapter    → lista observable de ítems
 */
class MainFragment : BrowseSupportFragment() {

    private val viewModel: TvViewModel by viewModels {
        TvViewModelFactory(requireContext().applicationContext)
    }

    /** Adapter mutable de la fila de historial — se actualiza desde el Flow de Room */
    private lateinit var histAdapter: ArrayObjectAdapter

    /** Adapter mutable de la fila de estado actual — se actualiza con el FC en vivo */
    private lateinit var estadoAdapter: ArrayObjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración del BrowseFragment
        title        = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        // Color de marca en el sidebar
        brandColor            = resources.getColor(R.color.sh_primary, null)
        searchAffordanceColor = resources.getColor(R.color.sh_amber, null)

        cargarFilas()
        observarDatos()
    }

    private fun cargarFilas() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

        // ── Fila 1: Estado actual (FC en tiempo real) ─────────────────
        estadoAdapter = ArrayObjectAdapter(FCCardPresenter())
        // Valor inicial mientras llegan datos del Repository
        estadoAdapter.add(LecturaFC(valorBpm = 0, hora = "Cargando..."))
        rowsAdapter.add(ListRow(HeaderItem("Estado actual"), estadoAdapter))

        // ── Fila 2: Historial FC desde Room ───────────────────────────
        histAdapter = ArrayObjectAdapter(FCCardPresenter())
        rowsAdapter.add(ListRow(HeaderItem("Historial FC"), histAdapter))

        this.adapter = rowsAdapter
    }

    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observar FC actual y actualizar la primera card de "Estado actual"
                launch {
                    viewModel.fc.collect { bpm ->
                        estadoAdapter.clear()
                        estadoAdapter.add(LecturaFC(valorBpm = bpm, hora = "Ahora"))
                    }
                }

                // Observar historial de Room y actualizar la fila completa
                launch {
                    viewModel.historial.collect { lecturas ->
                        histAdapter.clear()
                        lecturas.forEach { histAdapter.add(it) }
                    }
                }
            }
        }
    }
}
