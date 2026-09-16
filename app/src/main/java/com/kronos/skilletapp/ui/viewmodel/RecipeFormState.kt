package com.kronos.skilletapp.ui.viewmodel

import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction

data class RecipeFormState(
  val name: String = "",
  val description: String = "",
  val notes: String = "",
  val servings: Int = 0,
  val prepTime: Int = 0,
  val cookTime: Int = 0,
  val source: String = "",
  val sourceName: String = "",
  val image: String? = null,
  val ingredients: List<Ingredient> = emptyList(),
  val instructions: List<Instruction> = emptyList(),
  val equipment: List<Equipment> = emptyList(),
  val userMessage: String? = null,
  val isRecipeSaved: Boolean = false,
  val tharBeChanges: Boolean = false,
  val isSaveInProgress: Boolean = false,
)
