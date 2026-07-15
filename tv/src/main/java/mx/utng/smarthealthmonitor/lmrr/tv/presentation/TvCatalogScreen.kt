@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)
package mx.utng.smarthealthmonitor.lmrr.tv.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.utng.smarthealthmonitor.lmrr.tv.LecturaFC
import mx.utng.smarthealthmonitor.lmrr.tv.TvViewModel

@Composable
fun TvCatalogScreen(
    onCardClick: (Int) -> Unit,
    viewModel: TvViewModel = viewModel()
) {
    val historial by viewModel.historial.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B4A))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Catálogo de Lecturas",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        if (historial.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay datos en el historial", color = Color.White)
            }
        } else {
            // Fila 1: Todas las lecturas
            Text(text = "Historial Completo", style = MaterialTheme.typography.titleLarge, color = Color.LightGray)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historial) { lectura ->
                    FcCardItem(lectura = lectura, onClick = { onCardClick(lectura.id) })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fila 2: Lecturas Anormales
            val anormales = historial.filter { !it.esNormal }
            if (anormales.isNotEmpty()) {
                Text(text = "Alertas Anormales", style = MaterialTheme.typography.titleLarge, color = Color.Red.copy(alpha = 0.8f))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(anormales) { lectura ->
                        FcCardItem(lectura = lectura, onClick = { onCardClick(lectura.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun FcCardItem(lectura: LecturaFC, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(width = 200.dp, height = 120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1565C0))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${lectura.valorBpm} bpm",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lectura.hora,
                fontSize = 16.sp,
                color = Color.LightGray
            )
        }
    }
}
