package mx.utng.smarthealthmonitor.lmrr.tv

import androidx.leanback.app.BrowseSupportFragment

/**
 * MainFragment — punto de entrada de la UI en Android TV.
 * Extiende BrowseSupportFragment de Leanback, que proporciona
 * la navegación lateral (BrowseFragment) con soporte D-pad.
 *
 * En pasos posteriores se agregarán filas de datos reales.
 */
class MainFragment : BrowseSupportFragment() {
    override fun onStart() {
        super.onStart()
        title = "SmartHealth TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = requireContext().getColor(R.color.sh_primary)
        searchAffordanceColor = requireContext().getColor(R.color.sh_amber)
    }
}
