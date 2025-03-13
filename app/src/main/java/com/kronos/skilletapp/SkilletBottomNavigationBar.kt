package com.kronos.skilletapp

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

enum class BottomNavigationBarVisibility {
  Hidden,
  Visible;

  fun isVisible() = this == Visible
  fun isHidden() = this == Hidden
}

class SkilletBottomNavigationBarState(
  initialVisibility: BottomNavigationBarVisibility = BottomNavigationBarVisibility.Visible,
) {
  var visibility by mutableStateOf(initialVisibility)
    private set

  fun hide() {
    visibility = BottomNavigationBarVisibility.Hidden
  }

  fun show() {
    visibility = BottomNavigationBarVisibility.Visible
  }

  val isVisible get() = visibility.isVisible()
  val isHidden get() = visibility.isHidden()

  companion object {
    val Saver = Saver<SkilletBottomNavigationBarState, BottomNavigationBarVisibility>(
      save = { it.visibility },
      restore = { SkilletBottomNavigationBarState(initialVisibility = it) }
    )
  }
}

@Composable
fun rememberBottomNavigationBarState(
  initialVisibility: BottomNavigationBarVisibility = BottomNavigationBarVisibility.Visible
): SkilletBottomNavigationBarState {
  return rememberSaveable(
    initialVisibility,
    saver = SkilletBottomNavigationBarState.Saver
  ) { SkilletBottomNavigationBarState(initialVisibility) }
}

val LocalSkilletBottomNavigationBarVisibility = compositionLocalOf<BottomNavigationBarVisibility> { error("No BottomNavigationBarVisibility provided")}
val LocalSkilletBottomNavigationBarState = compositionLocalOf<SkilletBottomNavigationBarState> { error("No BottomNavigationBarState provided")}

@Composable
fun SkilletBottomNavigationBar(
  state: SkilletBottomNavigationBarState = rememberBottomNavigationBarState(),
  navController: NavHostController,
  navActions: SkilletNavigationActions,
) {
  if (state.isVisible) {
    NavigationBar(
      containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
      val screens = SkilletBottomNavigationBarItems.values
      var selectedScreen by remember { mutableStateOf<SkilletBottomNavigationBarItems<*>>(SkilletBottomNavigationBarItems.RecipeList) }

      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentDestination = navBackStackEntry?.destination

      screens.find { currentDestination?.route?.contains(it.route::class.qualifiedName.toString()) == true }?.let {
        selectedScreen = it.takeUnless { selectedScreen == it } ?: selectedScreen
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
        )
      }
    }
  }
}

internal sealed class SkilletBottomNavigationBarItems<T : Route>(
  val label: String,
  val route: T,
  val icon: ImageVector,
  private val selectedIcon: ImageVector,
) {
  @Composable
  fun Icon(isSelected: Boolean) {
    AnimatedContent(
      targetState = isSelected,
      transitionSpec = {
        fadeIn() togetherWith fadeOut()
      }
    ) {
      if (it) {
        androidx.compose.material3.Icon(imageVector = selectedIcon, contentDescription = null)
      } else {
        androidx.compose.material3.Icon(imageVector = icon, contentDescription = null)
      }
    }
  }

  data object RecipeList : SkilletBottomNavigationBarItems<Route.RecipeList>(
    label = "Recipes",
    route = Route.RecipeList(),
    icon = Icons.AutoMirrored.Outlined.ListAlt,
    selectedIcon = Icons.AutoMirrored.Filled.ListAlt
  )

  companion object {
    val values by lazy {
      listOf(
        RecipeList,
      )
    }
  }
}