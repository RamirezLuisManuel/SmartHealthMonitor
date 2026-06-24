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
            WearApp()
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
fun WearApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Variable para mostrar los latidos en la pantalla del reloj
    var bpmText by remember { mutableStateOf("---") }

    // 1. Creamos el Callback que se ejecutará CADA SEGUNDO cuando haya un nuevo dato
    val measureCallback = remember {
        object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                Log.d("MeasureClient", "Disponibilidad del sensor: $availability")
            }

            override fun onDataReceived(data: DataPointContainer) {
                val fcDataPoints = data.getData(DataType.HEART_RATE_BPM)
                val ultimoDatoFC = fcDataPoints.lastOrNull()

                if (ultimoDatoFC is SampleDataPoint<Double>) {
                    val bpm = ultimoDatoFC.value.toInt()
                    // Usamos un valor temporal para forzar la actualización si es el mismo
                    Log.d("MeasureClient", "Dato instantáneo del sensor: $bpm")
                    bpmText = bpm.toString()

                    // Enviar al teléfono inmediatamente
                    scope.launch {
                        val sender = WearDataSender(context)
                        sender.enviarFC(bpm)
                    }
                }
            }
        }
    }

    SmartHealthMonitorTheme {
        AppScaffold {
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

                    // Muestra los latidos en tiempo real en la pantalla
                    Text(
                        text = "$bpmText BPM",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            // 2. Registramos el cliente para empezar a escuchar en tiempo real
                            val healthClient = HealthServices.getClient(context)
                            healthClient.measureClient.registerMeasureCallback(
                                DataType.HEART_RATE_BPM,
                                measureCallback
                            )
                            Log.d("MeasureClient", "Escuchando en tiempo real iniciado")
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    ) {
                        Text("LEER SENSOR EN VIVO", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@Composable
fun DefaultPreview() {
    WearApp()
}