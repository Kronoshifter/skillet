package com.kronos.skilletapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.util.Consumer
import androidx.navigation.compose.rememberNavController
import com.kronos.skilletapp.navigation.LocalNavController
import com.kronos.skilletapp.navigation.LocalNavigationActions
import com.kronos.skilletapp.navigation.SkilletNavGraph
import com.kronos.skilletapp.navigation.SkilletNavigationActions
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.androidx.compose.KoinAndroidContext

class MainActivity : ComponentActivity() {

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    if (savedInstanceState == null) {
      Log.d("SHARED_RECIPE", "Cold Start")
      Log.d("SHARED_RECIPE", "Intent: ${intent.action}, ${intent.hasExtra(Intent.EXTRA_TEXT)}, ${intent.extras}")
    }

    setContent {
      SkilletAppTheme {
        KoinAndroidContext {
          val navController = rememberNavController()
          val navActions = remember(navController) { SkilletNavigationActions(navController) }

          val newIntent by produceState(intent.takeIf { it.action == Intent.ACTION_SEND }) {
            val consumer = Consumer<Intent> { intent ->
              Log.d("SHARED_RECIPE", "New Intent: ${intent.action}")
              value = intent
            }

            addOnNewIntentListener(consumer)
            awaitDispose {
              removeOnNewIntentListener(consumer)
            }
          }

          Surface {
            CompositionLocalProvider(
              LocalNavigationActions provides navActions,
              LocalNavController provides navController
            ) {
              SkilletNavGraph(
                newIntent = newIntent,
                navController = navController,
                navActions = navActions,
                modifier = Modifier
                  .fillMaxSize(),
              )
            }
          }
        }
      }
    }
  }
}