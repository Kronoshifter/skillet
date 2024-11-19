package com.kronos.skilletapp.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kronos.skilletapp.database.RecipeConverters
import kotlinx.serialization.Serializable

@Entity(tableName = "recipe")
@TypeConverters(RecipeConverters::class)
@Serializable
data class Recipe(
  @PrimaryKey val id: String,
  val name: String,
  val description: String,
  val cover: String? = null, // cover photo
  val notes: String,
  val servings: Int,
  @Embedded val time: RecipeTime,
  @Embedded val source: RecipeSource,
  val ingredients: List<Ingredient>, // TODO: convert to ingredient sections
  val instructions: List<Instruction>, // TODO: convert to instruction sections
  val equipment: List<Equipment>
) {
//  val allIngredients by lazy { ingredients.flatMap { it.ingredients } }
//  val allInstructions by lazy { instructions.flatMap { it.instructions } }
}

@Serializable
data class RecipeTime(
  @ColumnInfo(name = "prep_time") val preparation: Int = 0,
  @ColumnInfo(name = "cook_time") val cooking: Int = 0,
) {

  val total: Int
    get() = preparation + cooking
}

@Serializable
data class RecipeSource(
  @ColumnInfo(name = "source_name") val name: String = "",
  @ColumnInfo(name = "source_url") val source: String = ""
)