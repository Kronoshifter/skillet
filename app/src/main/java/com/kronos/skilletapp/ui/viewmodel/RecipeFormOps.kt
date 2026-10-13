package com.kronos.skilletapp.ui.viewmodel

import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction

/** Scalar field mutations for the add/edit form: each operation updates one field of the recipe-form state and then reports the change. */
interface RecipeFormFieldOps {
  fun updateName(name: String)

  fun updateDescription(description: String)

  fun updateNotes(notes: String)

  fun updateServings(servings: Int)

  fun updatePrepTime(prepTime: Int)

  fun updateCookTime(cookTime: Int)

  fun updateSource(source: String)

  fun updateSourceName(sourceName: String)

  fun updateImage(image: String?)

  fun showMessage(message: String)

  fun userMessageShown()
}

/** Collection mutations (update/remove/move) for the add/edit form's ingredients, instructions, and equipment. */
interface RecipeFormCollectionOps {
  fun updateIngredient(ingredient: Ingredient)

  fun removeIngredient(ingredient: Ingredient)

  fun moveIngredient(from: Int, to: Int)

  fun updateInstruction(instruction: Instruction)

  fun removeInstruction(instruction: Instruction)

  fun moveInstruction(from: Int, to: Int)

  fun updateEquipment(equipment: Equipment)

  fun removeEquipment(equipment: Equipment)

  fun moveEquipment(from: Int, to: Int)
}

@Suppress("EmptyFunctionBlock")
object PreviewRecipeFormFieldOps : RecipeFormFieldOps {
  override fun updateName(name: String) {}

  override fun updateDescription(description: String) {}

  override fun updateNotes(notes: String) {}

  override fun updateServings(servings: Int) {}

  override fun updatePrepTime(prepTime: Int) {}

  override fun updateCookTime(cookTime: Int) {}

  override fun updateSource(source: String) {}

  override fun updateSourceName(sourceName: String) {}

  override fun updateImage(image: String?) {}

  override fun showMessage(message: String) {}

  override fun userMessageShown() {}
}

@Suppress("EmptyFunctionBlock")
object PreviewRecipeFormCollectionOps : RecipeFormCollectionOps {
  override fun updateIngredient(ingredient: Ingredient) {}

  override fun removeIngredient(ingredient: Ingredient) {}

  override fun moveIngredient(from: Int, to: Int) {}

  override fun updateInstruction(instruction: Instruction) {}

  override fun removeInstruction(instruction: Instruction) {}

  override fun moveInstruction(from: Int, to: Int) {}

  override fun updateEquipment(equipment: Equipment) {}

  override fun removeEquipment(equipment: Equipment) {}

  override fun moveEquipment(from: Int, to: Int) {}
}
