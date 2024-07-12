package com.kronos.skilletapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kronos.skilletapp.model.Recipe

@Composable
fun RecipeListScreen(
  onRecipeClick: (/*Recipe*/) -> Unit
) {
  Box(modifier = Modifier.fillMaxSize()) {
    Button(
      onClick = { onRecipeClick() },
      modifier = Modifier.align(Alignment.Center)
    ) {
      Text(text = "Open Recipe")
    }
  }
}