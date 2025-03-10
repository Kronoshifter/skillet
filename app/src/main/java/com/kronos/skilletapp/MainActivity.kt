package com.kronos.skilletapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
  navController: NavHostController = rememberNavController(),
  navActions: SkilletNavigationActions = remember(navController) { SkilletNavigationActions(navController) }
) {
  NavigationBar(
//    containerColor = MaterialTheme.colorScheme.primary
  ) {
    val screens = BottomNavItems.values

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    screens.forEach { screen ->
      val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route::class.qualifiedName } == true
      NavigationBarItem(
        icon = {
          Icon(
            imageVector = screen.icon,
            contentDescription = null
          )
        },
        label = {
          Text(text = screen.label)
        },
        selected = isSelected,
        onClick = {
          navActions.navigateViaBottomNav(screen.route)
        },
        colors = NavigationBarItemDefaults.colors()
      )
    }
  }
}