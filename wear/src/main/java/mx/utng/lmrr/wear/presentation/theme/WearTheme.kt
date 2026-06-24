package mx.utng.lmrr.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun SmartHealthWearTheme(
    content: @Composable () -> Unit
) {
    // Wear Material Theme — versión circular de MD3
    MaterialTheme(
        content = content
    )
}