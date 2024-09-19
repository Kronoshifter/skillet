package com.kronos.skilletapp.ui.screen.cooking

import androidx.compose.runtime.Composable
import com.kronos.skilletapp.ui.viewmodel.CookingViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun LoadingScreen(
  onBack: () -> Unit,
  vm: CookingViewModel = getViewModel(),
) {

}