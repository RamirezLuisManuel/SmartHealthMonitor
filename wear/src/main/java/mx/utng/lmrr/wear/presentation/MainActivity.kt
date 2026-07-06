package mx.utng.lmrr.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import mx.utng.lmrr.wear.health.HealthDataService
import mx.utng.lmrr.wear.presentation.theme.SmartHealthWearTheme

class MainActivity : ComponentActivity() {

    // Registramos UN SOLO lanzador para pedir múltiples permisos a la vez
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val bodySensorsGranted = permisos[Manifest.permission.BODY_SENSORS] ?: false

        if (bodySensorsGranted) {
            Log.d("MainActivity", "Permiso de sensores concedido por el usuario.")
            registrarServicioSalud()
        } else {
            Log.w("MainActivity", "Permiso denegado. Los sensores virtuales no enviarán datos.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tieneBodySensors = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
        val tieneActivityRec = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED

        if (tieneBodySensors && tieneActivityRec) {
            Log.d("MainActivity", "Los permisos ya estaban concedidos.")
            registrarServicioSalud()
        } else {
            Log.d("MainActivity", "Solicitando permisos al usuario...")
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }

        setContent {
            SmartHealthWearTheme {
                // TODO Ej.02 completado: usar SmartHealthWearNavGraph
                SmartHealthWearNavGraph()
            }
        }
    }

    private fun registrarServicioSalud() {
        lifecycleScope.launch {
            try {
                HealthDataService.registrar(applicationContext)
                Log.d("MainActivity", "HealthDataService registrado exitosamente.")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error al registrar HealthDataService", e)
            }
        }
    }
}