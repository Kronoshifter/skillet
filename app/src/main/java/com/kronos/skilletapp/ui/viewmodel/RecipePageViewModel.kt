package com.kronos.skilletapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.MeasurementUnit

class RecipePageViewModel : ViewModel() {
  val selectedUnits = mutableStateMapOf<Ingredient, MeasurementUnit?>()
}