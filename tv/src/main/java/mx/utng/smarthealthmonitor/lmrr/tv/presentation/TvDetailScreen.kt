@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package mx.utng.smarthealthmonitor.lmrr.tv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.utng.smarthealthmonitor.lmrr.tv.TvViewModel

@Composable
fun TvDetailScreen(
    lecturaId   : Int,
    navController: NavController,
    viewModel   : TvViewModel = viewModel()
) {
    val historial by viewModel.historial.collectAsStateWithLifecycle()
    // Buscar la lectura específica en el historial
    val lectura = historial.find { it.id == lecturaId }
 
    val focusRequester = remember { FocusRequester() }

    // Solicitar foco al botón de reproducir cuando la lectura cargue
    LaunchedEffect(lectura) {
        if (lectura != null) {
            focusRequester.requestFocus()
        }
    }

    if (lectura == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0D1B4A)),
            contentAlignment = Alignment.Center
        ) {
            Text("Cargando detalles de la lectura...", color = Color.White)
        }
        return
    }
 
    Row(Modifier.fillMaxSize().background(Color(0xFF0D1B4A)).padding(64.dp),
        horizontalArrangement = Arrangement.spacedBy(48.dp)) {
 
        // Panel izquierdo — ícono + datos
        Column(Modifier.weight(0.4f), verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(200.dp).background(Color(0xFF1565C0),CircleShape),
                contentAlignment=Alignment.Center) {
                Text("❤", fontSize = 80.sp)
            }
            Text("${lectura.valorBpm} bpm",
                 style=MaterialTheme.typography.displayMedium,
                 color=Color.White, fontWeight=FontWeight.ExtraBold)
            Text("Estado: ${if (lectura.esNormal) "Normal" else "Anormal"}",
                 style=MaterialTheme.typography.bodyLarge, color=Color.White.copy(0.8f))
            Text("Hora: ${lectura.hora}",
                 style=MaterialTheme.typography.bodyMedium, color=Color.White.copy(0.6f))
        }
 
        // Panel derecho — botones de acción
        Column(Modifier.weight(0.6f), verticalArrangement=Arrangement.spacedBy(20.dp),
               horizontalAlignment=Alignment.CenterHorizontally) {
 
            Spacer(Modifier.weight(1f))
 
            Button(onClick = { navController.navigate("playback") },
                    modifier=Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(0.7f).height(60.dp),
                    colors=ButtonDefaults.colors(
                        containerColor=Color(0xFF1B5E20),
                        focusedContainerColor=Color(0xFF76FF03)),
                    shape=ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))) {
                Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                    Text("▶  Reproducir",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Bold)
                }
            }
 
            Button(onClick = { navController.popBackStack() },
                    modifier=Modifier.fillMaxWidth(0.7f).height(60.dp),
                    colors=ButtonDefaults.colors(
                        containerColor=Color(0xFF37474F),
                        focusedContainerColor=Color(0xFF90A4AE)),
                    shape=ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))) {
                Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
                    Text("← Volver",color=Color.White,fontSize=18.sp)
                }
            }
 
            Spacer(Modifier.weight(1f))
        }
    }
}
