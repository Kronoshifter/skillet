package com.kronos.skilletapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kronos.skilletapp.model.*
import com.kronos.skilletapp.ui.RecipeContent
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