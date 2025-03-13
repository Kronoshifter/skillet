package com.kronos.skilletapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
  private val intentFlow = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      KoinContext() {
        SkilletAppTheme {
          val navController = rememberNavController()
          val navActions = remember(navController) { SkilletNavigationActions(navController) }

          Scaffold(
            bottomBar = {
              BottomNavigationBar(navController = navController, navActions = navActions)
            },
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(TopAppBarDefaults.windowInsets)
          ) { padding ->
            SkilletNavGraph(
              intentFlow = intentFlow,
              modifier = Modifier
                .fillMaxSize()
                .padding(padding),
              navController = navController,
              navActions = navActions
            )
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    intentFlow.tryEmit(intent)
  }
}

@Composable
fun BottomNavigationBar(
  navController: NavHostController,
  navActions: SkilletNavigationActions,
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surfaceContainer
  ) {
    val screens = BottomNavItems.values
    var selectedScreen by remember { mutableStateOf<BottomNavItems<*>>(BottomNavItems.RecipeList) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    screens.find { currentDestination?.route?.contains(it.route::class.qualifiedName.toString()) == true }?.let {
      selectedScreen = it
    }

    screens.forEach { screen ->
      val isSelected = screen == selectedScreen
      NavigationBarItem(
        icon = {
          screen.Icon(isSelected)
        },
        label = {
          Text(text = screen.label)
        },
        selected = isSelected,
        onClick = {
          selectedScreen = screen
          navActions.navigateViaBottomNav(screen.route)
        },
        colors = NavigationBarItemDefaults.colors()
      )
    }
  }
}