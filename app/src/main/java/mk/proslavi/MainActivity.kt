package mk.proslavi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mk.proslavi.data.ProslaviDao
import mk.proslavi.ui.EventScreen
import mk.proslavi.ui.EventsScreen
import mk.proslavi.ui.theme.ProslaviTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dao = (application as ProslaviApp).database.dao()
        setContent {
            ProslaviTheme {
                AppNavigation(dao)
            }
        }
    }
}

@Composable
private fun AppNavigation(dao: ProslaviDao) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "events") {
        composable("events") {
            val vm: EventsViewModel = viewModel(factory = viewModelFactory { initializer { EventsViewModel(dao) } })
            EventsScreen(vm, onOpen = { id -> nav.navigate("event/$id") })
        }
        composable("event/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val vm: EventViewModel = viewModel(factory = viewModelFactory { initializer { EventViewModel(dao, id) } })
            EventScreen(vm, onBack = { nav.popBackStack() })
        }
    }
}
