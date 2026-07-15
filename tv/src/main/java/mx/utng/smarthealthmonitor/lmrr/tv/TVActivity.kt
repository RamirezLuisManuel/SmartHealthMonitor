package mx.utng.smarthealthmonitor.lmrr.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.utng.smarthealthmonitor.lmrr.tv.presentation.TvCatalogScreen
import mx.utng.smarthealthmonitor.lmrr.tv.presentation.TvDetailScreen
import mx.utng.smarthealthmonitor.lmrr.tv.presentation.TvPlaybackScreen
import mx.utng.smarthealthmonitor.lmrr.tv.ui.theme.SmartHealthTvTheme

/**
 * Actividad principal para Android TV usando Jetpack Compose.
 */
class TVActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SmartHealthTvTheme {
                val navController = rememberNavController()
                
                // Se utiliza el mismo ViewModel para todo el grafo de navegación
                val tvViewModel: TvViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "catalog") {
                    
                    // 1. Pantalla de Catálogo (Lista de Cards)
                    composable("catalog") {
                        TvCatalogScreen(
                            onCardClick = { lecturaId ->
                                navController.navigate("detail/$lecturaId")
                            },
                            viewModel = tvViewModel
                        )
                    }
                    
                    // 2. Pantalla de Detalle
                    composable(
                        route = "detail/{lecturaId}",
                        arguments = listOf(navArgument("lecturaId") { type = NavType.IntType })
                    ) { backStack ->
                        val id = backStack.arguments?.getInt("lecturaId") ?: return@composable
                        TvDetailScreen(
                            lecturaId = id, 
                            navController = navController,
                            viewModel = tvViewModel
                        )
                    }
                    
                    // 3. Pantalla de Reproducción (Video)
                    composable("playback") {
                        TvPlaybackScreen(navController = navController)
                    }
                }
            }
        }
    }
}
