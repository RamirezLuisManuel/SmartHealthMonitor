package mx.utng.lmrr.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material3.*
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import kotlinx.coroutines.launch
import mx.utng.lmrr.wear.data.WearDataSender
import mx.utng.lmrr.wear.health.HealthDataService
import mx.utng.lmrr.wear.presentation.theme.SmartHealthMonitorTheme
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.SampleDataPoint
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    // 1. Registramos UN SOLO lanzador para pedir múltiples permisos a la vez
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        // Revisamos si el usuario nos dio permiso de los sensores
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

        // 2. Verificamos si los permisos ya fueron concedidos previamente
        val tieneBodySensors = ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
        val tieneActivityRec = ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED

        if (tieneBodySensors && tieneActivityRec) {
            // Si ya tenemos los permisos, registramos el servicio directo
            Log.d("MainActivity", "Los permisos ya estaban concedidos.")
            registrarServicioSalud()
        } else {
            // Si falta alguno, lanzamos la petición al usuario
            Log.d("MainActivity", "Solicitando permisos al usuario...")
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }

        // 3. Mostramos la interfaz de Compose (sin importar si ya hay permisos o apenas se pedirán)
        setContent {
            WearDashboardScreen()
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

@Composable
fun WearDashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Aquí mantenemos el botón que ya nos funcionaba para probar la conexión
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Corazón",
                tint = Color.Red,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SmartHealth Wear",
                style = MaterialTheme.typography.titleMedium, // Tipografía adaptada para Wear
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val sender = WearDataSender(context)
                    scope.launch {
                        sender.enviarFC(120)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Text("SIMULAR 120 BPM", fontSize = 10.sp)
            }
        }
    }
}