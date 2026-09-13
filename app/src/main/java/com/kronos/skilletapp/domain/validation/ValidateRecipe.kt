package com.kronos.skilletapp.domain.validation

import com.github.michaelbull.result.Result
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.InvalidFormError
import com.kronos.skilletapp.utils.err
import com.kronos.skilletapp.utils.ok

class ValidateRecipe {
  operator fun invoke(
    name: String,
    ingredients: List<Ingredient>,
    instructions: List<Instruction>,
    servings: Int,
    cookTime: Int,
  ): Result<Unit, InvalidFormError> {
    return when {
      name.isBlank() -> InvalidFormError("Name cannot be blank").err()
      ingredients.isEmpty() -> InvalidFormError("At least one ingredient is required").err()
      instructions.isEmpty() -> InvalidFormError("At least one instruction is required").err()
      servings <= 0 -> InvalidFormError("Servings must be greater than 0").err()
      cookTime <= 0 -> InvalidFormError("Cook time must be greater than 0").err()
      else -> Unit.ok()
    }
  }
}
