package com.kronos.skilletapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kronos.skilletapp.ui.theme.SkilletAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      SkilletAppTheme {
        SkilletNavGraph()
      }
    }
  }
}