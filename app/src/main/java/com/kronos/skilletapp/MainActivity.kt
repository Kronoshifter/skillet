package com.kronos.skilletapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kronos.skilletapp.ui.theme.SkilletAppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
  private val intentFlow = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      KoinContext() {
        SkilletAppTheme {
          SkilletNavGraph(intentFlow = intentFlow)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    intentFlow.tryEmit(intent)
  }
}