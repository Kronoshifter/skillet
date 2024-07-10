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
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          val ingredients = listOf(
            Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce)),
            Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
            Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
            Ingredient(
              "Garlic",
              IngredientType.Dry,
              measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))
            ),
            Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
            Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
            Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
          )

          val instructions = listOf(
            Instruction(
              text = "Cook pasta in a pot of salted boiling water until al dente",
              ingredients = listOf(
                Ingredient("Mini Shells Pasta", IngredientType.Dry, measurement = Measurement(8.0, MeasurementUnit.Ounce)),
              )
            ),
            Instruction(
              text = "Return pot to stove over medium heat then ass butter and olive oil. Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. Sprinkle in flour then whisk and saute for 1 minute. Slowly pour in chicken broth and milk while whisking until mixture is smooth. Season with salt and pepper then switch to a wooden spoon and stir constantly until mixture is thick and bubbly, 4.-5 minutes.",
              ingredients = listOf(
                Ingredient("Butter", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
                Ingredient("Olive Oil", IngredientType.Wet, measurement = Measurement(1.0, MeasurementUnit.Tablespoon)),
                Ingredient(
                  "Garlic",
                  IngredientType.Dry,
                  measurement = Measurement(2.0, MeasurementUnit.Custom("clove", "clove"))
                ),
                Ingredient("Flour", IngredientType.Dry, measurement = Measurement(2.0, MeasurementUnit.Tablespoon)),
                Ingredient("Chicken Broth", IngredientType.Wet, measurement = Measurement(0.75, MeasurementUnit.Cup)),
                Ingredient("Milk", IngredientType.Wet, measurement = Measurement(2.5, MeasurementUnit.Cup)),
              )
            ),
            Instruction(
              text = "Remove pot from heat then stir in parmesan cheese, garlic powder, and parsley flakes until smooth. Add cooked pasta then stir to combine. Taste then adjust salt and pepper if necessary, and then serve.",
              ingredients = emptyList()
            ),
          )

          val recipe = Recipe(
            name = "Creamy Garlic Pasta Shells",
            ingredients = ingredients,
            instructions = instructions,
            equipment = emptyList(),
            servings = 4,
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            time = RecipeTime(15, 15),
            source = RecipeSource("My Brain", "My Brain"),
            notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
          )

          RecipeContent(recipe = recipe)
        }
      }
    }
  }
}