package mx.utng.smarthealthmonitor.lmrr.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

/**
 * MainActivity para Android TV.
 * Contenedor principal: inicializa el Repository y carga MainFragment.
 * TODA la lógica de UI va en el Fragment.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Repository con contexto (necesario para Room)
        SmartHealthRepository.init(applicationContext)

        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commit()
        }
    }
}