package com.kronos.skilletapp.data

import com.kronos.skilletapp.database.entity.EquipmentEntity
import com.kronos.skilletapp.database.entity.IngredientEntity
import com.kronos.skilletapp.database.entity.InstructionEntity
import com.kronos.skilletapp.database.entity.InstructionEquipmentEntity
import com.kronos.skilletapp.database.entity.InstructionIngredientEntity
import com.kronos.skilletapp.database.entity.RecipeEntity
import com.kronos.skilletapp.model.Equipment
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeSource
import com.kronos.skilletapp.model.RecipeTime

/**
 * A flat bundle of every entity row that a single domain [Recipe] maps to: the `recipe` row, the three child-table rows (ordered by their
 * stored `position`), and the two join-table rows. The later write path upserts all of these inside one `RoomDatabase.withTransaction`; the
 * read path assembles a [MappedRecipe] from the per-table DAOs and hands it to [RecipeMapper.toDomain].
 */
data class MappedRecipe(
  val recipe: RecipeEntity,
  val ingredients: List<IngredientEntity>,
  val instructions: List<InstructionEntity>,
  val equipment: List<EquipmentEntity>,
  val instructionIngredients: List<InstructionIngredientEntity>,
  val instructionEquipment: List<InstructionEquipmentEntity>,
)

/**
 * Stateless mapper bridging the domain [Recipe] graph and its relational entity rows + join rows.
 *
 * Domain → entities ([toEntities]):
 * - The [Recipe] flattens to a single [RecipeEntity] (`time`/`source` VOs become scalar columns).
 * - Child rows are keyed by their domain `id`. Ingredients/equipment referenced at both the recipe level and an instruction level produce
 *   exactly ONE child row (deduped by id). `position` is the index in the source list where the id first appears: recipe-level ids take
 *   positions 0..n-1, and any id first seen only at an instruction level is appended after them (collision-free).
 * - Join rows carry `position` = the index within that specific instruction's ingredient/equipment list.
 *
 * Entities → domain ([toDomain]): reassembles the [Recipe] graph. Each instruction's ingredients and equipment are looked up by id from the
 * child rows, ordered by the join row's `position`; a shared id may therefore appear under multiple instructions.
 */
class RecipeMapper {

  fun toEntities(recipe: Recipe): MappedRecipe {
    val recipeId = recipe.id

    val ingredients =
      LinkedHashMap<String, IngredientEntity>().let { ingredientMap ->
        recipe.ingredients
          .distinctBy { it.id }
          .mapIndexed { position, ing -> ing.toEntity(recipeId, position) }
          .associateByTo(ingredientMap) { it.id }
      }

    val equipment =
      LinkedHashMap<String, EquipmentEntity>().let { equipmentMap ->
        recipe.equipment
          .distinctBy { it.id }
          .mapIndexed { position, eq -> eq.toEntity(recipeId, position) }
          .associateByTo(equipmentMap) { it.id }
      }

    val instructions = recipe.instructions.mapIndexed { position, ins -> ins.toEntity(recipeId, position) }

    val instructionIngredients =
      recipe.instructions.flatMap { ins ->
        ins.ingredients.mapIndexed { position, ing ->
          InstructionIngredientEntity(
            instructionId = ins.id,
            ingredientId = ing.id,
            recipeId = recipeId,
            position = position,
          )
        }
      }

    val instructionEquipment =
      recipe.instructions.flatMap { ins ->
        ins.equipment.mapIndexed { position, eq ->
          InstructionEquipmentEntity(
            instructionId = ins.id,
            equipmentId = eq.id,
            recipeId = recipeId,
            position = position,
          )
        }
      }

    return MappedRecipe(
      recipe =
        RecipeEntity(
          id = recipe.id,
          name = recipe.name,
          description = recipe.description,
          cover = recipe.cover,
          notes = recipe.notes,
          servings = recipe.servings,
          prepTime = recipe.time.preparation,
          cookTime = recipe.time.cooking,
          sourceName = recipe.source.name,
          sourceUrl = recipe.source.source,
        ),
      ingredients = ingredients.values.sortedBy { it.position },
      instructions = instructions,
      equipment = equipment.values.sortedBy { it.position },
      instructionIngredients = instructionIngredients,
      instructionEquipment = instructionEquipment,
    )
  }

  fun toDomain(mapped: MappedRecipe): Recipe {
    val ingredientById = mapped.ingredients.associateBy { it.id }
    val equipmentById = mapped.equipment.associateBy { it.id }

    // Group join rows per instruction and order each group by the join row's position, resolving
    // the referenced child rows by id. A shared id may appear under multiple instructions.
    val ingredientsByInstruction =
      mapped.instructionIngredients
        .groupBy { it.instructionId }
        .mapValues { (_, rows) ->
          rows
            .sortedBy { it.position }
            .map { requireNotNull(ingredientById[it.ingredientId]) { "dangling ingredient ref: ${it.ingredientId}" }.toDomain() }
        }
    val equipmentByInstruction =
      mapped.instructionEquipment
        .groupBy { it.instructionId }
        .mapValues { (_, rows) ->
          rows
            .sortedBy { it.position }
            .map { requireNotNull(equipmentById[it.equipmentId]) { "dangling equipment ref: ${it.equipmentId}" }.toDomain() }
        }

    return Recipe(
      id = mapped.recipe.id,
      name = mapped.recipe.name,
      description = mapped.recipe.description,
      cover = mapped.recipe.cover,
      notes = mapped.recipe.notes,
      servings = mapped.recipe.servings,
      time = RecipeTime(mapped.recipe.prepTime, mapped.recipe.cookTime),
      source = RecipeSource(mapped.recipe.sourceName, mapped.recipe.sourceUrl),
      ingredients = mapped.ingredients.sortedBy { it.position }.map { it.toDomain() },
      instructions =
        mapped.instructions
          .sortedBy { it.position }
          .map {
            it.toDomain(
              equipment = equipmentByInstruction[it.id].orEmpty(),
              ingredients = ingredientsByInstruction[it.id].orEmpty(),
            )
          },
      equipment = mapped.equipment.sortedBy { it.position }.map { it.toDomain() },
    )
  }

  private fun Ingredient.toEntity(
    recipeId: String,
    position: Int,
  ): IngredientEntity =
    IngredientEntity(
      id = id,
      recipeId = recipeId,
      position = position,
      name = name,
      measurement = measurement,
      raw = raw,
      comment = comment,
    )

  private fun IngredientEntity.toDomain(): Ingredient =
    Ingredient(
      name = name,
      measurement = measurement,
      raw = raw,
      comment = comment,
      id = id,
    )

  private fun Equipment.toEntity(
    recipeId: String,
    position: Int,
  ): EquipmentEntity =
    EquipmentEntity(
      id = id,
      recipeId = recipeId,
      position = position,
      name = name,
    )

  private fun EquipmentEntity.toDomain(): Equipment = Equipment(name = name, id = id)

  private fun Instruction.toEntity(recipeId: String, i: Int): InstructionEntity =
    InstructionEntity(
      id = id,
      recipeId = recipeId,
      position = i,
      text = text,
      image = image,
    )

  private fun InstructionEntity.toDomain(
    equipment: List<Equipment>,
    ingredients: List<Ingredient>,
  ): Instruction =
    Instruction(
      text = text,
      image = image,
      equipment = equipment,
      ingredients = ingredients,
      id = id,
    )
}
