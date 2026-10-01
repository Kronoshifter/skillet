package com.kronos.skilletapp.data

import androidx.room3.withWriteTransaction
import com.kronos.measurement.model.Measurement
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.database.dao.EquipmentDao
import com.kronos.skilletapp.database.dao.IngredientDao
import com.kronos.skilletapp.database.dao.InstructionDao
import com.kronos.skilletapp.database.dao.RecipeDao
import com.kronos.skilletapp.model.Ingredient
import com.kronos.skilletapp.model.Instruction
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeSource
import com.kronos.skilletapp.model.RecipeSummary
import com.kronos.skilletapp.model.RecipeTime
import com.kronos.skilletapp.model.toSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
  private val database: RecipeDatabase,
  private val recipeDao: RecipeDao,
  private val ingredientDao: IngredientDao,
  private val instructionDao: InstructionDao,
  private val equipmentDao: EquipmentDao,
  private val mapper: RecipeMapper,
) : RecipeRepository {

  override suspend fun fetchRecipe(id: String) = loadRecipe(id)

  override fun observeRecipe(id: String): Flow<Recipe> = graphFlow(id).map(mapper::toDomain)

  override suspend fun fetchRecipes() = recipeDao.getAll().map { loadRecipe(it.id) }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun observeRecipes(): Flow<List<Recipe>> =
    recipeDao.observeAll().flatMapLatest { entities ->
      entities.map { entity -> graphFlow(entity.id).map(mapper::toDomain) }.combineFlows()
    }

  override fun observeRecipeSummaries(): Flow<List<RecipeSummary>> =
    recipeDao.observeAll().map { entities -> entities.map { it.toSummary() } }

  override suspend fun upsert(recipe: Recipe) {
    val mapped = mapper.toEntities(recipe)
    val id = recipe.id
    database.withWriteTransaction {
      // 1) existing join rows first (they reference the child rows that follow)
      instructionDao.deleteInstructionIngredientsByRecipeId(id)
      instructionDao.deleteInstructionEquipmentByRecipeId(id)
      // 2) existing child rows
      ingredientDao.deleteByRecipeId(id)
      instructionDao.deleteByRecipeId(id)
      equipmentDao.deleteByRecipeId(id)
      // 3) recipe row
      recipeDao.upsert(mapped.recipe)
      // 4) child rows
      ingredientDao.upsertAll(mapped.ingredients)
      instructionDao.upsertInstructions(mapped.instructions)
      equipmentDao.upsertAll(mapped.equipment)
      // 5) join rows last (they reference the child rows just written)
      instructionDao.upsertInstructionIngredients(mapped.instructionIngredients)
      instructionDao.upsertInstructionEquipment(mapped.instructionEquipment)
    }
  }

  override suspend fun createRecipe(recipe: Recipe): String {
    upsert(recipe)
    return recipe.id
  }

  override suspend fun updateRecipe(id: String, recipe: Recipe) = upsert(recipe.copy(id = id))

  /**
   * Suspends to assemble the full relational graph for a single recipe row from the per-table DAOs. The recipe row must exist; child and
   * join tables are always present (possibly empty) for a valid recipe, so only the recipe row is guarded.
   */
  private suspend fun loadRecipe(id: String): Recipe {
    val recipe = requireNotNull(recipeDao.getById(id)) { "recipe not found: $id" }
    return mapper.toDomain(
      MappedRecipe(
        recipe = recipe,
        ingredients = ingredientDao.getByRecipeId(id),
        instructions = instructionDao.getByRecipeId(id),
        equipment = equipmentDao.getByRecipeId(id),
        instructionIngredients = instructionDao.selectInstructionIngredients(id),
        instructionEquipment = instructionDao.selectInstructionEquipment(id),
      )
    )
  }

  /**
   * Reactive variant of [loadRecipe]: a cold [Flow] that re-emits the assembled graph whenever any table backing this recipe changes. All
   * six sources are combined so child/join edits (not just a recipe-row write) trigger a re-assembly.
   */
  private fun graphFlow(id: String): Flow<MappedRecipe> =
    combine(
      recipeDao.observeById(id),
      ingredientDao.observeByRecipeId(id),
      instructionDao.observeByRecipeId(id),
      equipmentDao.observeByRecipeId(id),
      combine(
        instructionDao.observeInstructionIngredients(id),
        instructionDao.observeInstructionEquipment(id),
      ) { iis, ieq ->
        iis to ieq
      },
    ) { recipe, ingredients, instructions, equipment, joins ->
      MappedRecipe(
        recipe = requireNotNull(recipe) { "recipe not found: $id" },
        ingredients = ingredients,
        instructions = instructions,
        equipment = equipment,
        instructionIngredients = joins.first,
        instructionEquipment = joins.second,
      )
    }

  //  init {
  //    runBlocking {
  //      initFakeRecipes()
  //    }
  //  }

  private suspend fun initFakeRecipes() {
    val ingredients =
      listOf(
        Ingredient(
          "Mini Shells Pasta",
          measurement = Measurement(8f, MeasurementUnit.Ounce),
          "8 oz Mini Shells Pasta",
        ),
        Ingredient(
          "Olive Oil",
          measurement = Measurement(1f, MeasurementUnit.Tablespoon),
          "1 tbsp Olive Oil",
        ),
        Ingredient(
          "Butter",
          measurement = Measurement(1f, MeasurementUnit.Tablespoon),
          "1 tbsp Butter",
        ),
        Ingredient(
          name = "Garlic",
          measurement = Measurement(2f, MeasurementUnit.Custom("clove")),
          raw = "2 cloves Garlic",
        ),
        Ingredient(
          "Flour",
          measurement = Measurement(2f, MeasurementUnit.Tablespoon),
          raw = "2 tbsp Flour",
        ),
        Ingredient(
          "Chicken Broth",
          measurement = Measurement(0.75f, MeasurementUnit.Cup),
          raw = "3/4 cup chicken broth",
        ),
        Ingredient(
          "Milk",
          measurement = Measurement(2.5f, MeasurementUnit.Cup),
          raw = "2 1/2 cups milk",
          comment = "separated",
        ),
        Ingredient(
          "Salt",
          measurement = Measurement(0f, MeasurementUnit.None),
          raw = "Salt, to taste",
          comment = "to taste",
        ),
      )

    val instructions =
      listOf(
        Instruction(
          text = "Cook pasta in a pot of salted boiling water until al dente",
          ingredients = ingredients.take(1),
        ),
        Instruction(
          text =
            "Return pot to stove over medium heat then ass butter and olive oil. " +
              "Once melted, add garlic then saute until light golden brown, about 30 seconds, being very careful not to burn. " +
              "Sprinkle in flour then whisk and saute for 1 minute. " +
              "Slowly pour in chicken broth and milk while whisking until mixture is smooth. " +
              "Season with salt and pepper then switch to a wooden spoon and " +
              "stir constantly until mixture is thick and bubbly, 4.-5 minutes.",
          ingredients = ingredients.slice(1..6),
        ),
        Instruction(
          text =
            "Remove pot from heat then stir in parmesan cheese, garlic powder, and parsley flakes until smooth. " +
              "Add cooked pasta then stir to combine. " +
              "Taste then adjust salt and pepper if necessary, and then serve.",
          ingredients = emptyList(),
        ),
      )

    val recipe =
      Recipe(
        id = "test",
        name = "Creamy Garlic Pasta Shells",
        ingredients = ingredients,
        instructions = instructions,
        equipment = emptyList(),
        servings = 4,
        description =
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        time = RecipeTime(15, 15),
        source = RecipeSource("My Brain", "My Brain"),
        notes =
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
      )

    upsert(recipe)

    val sampleRecipeCount = 10
    repeat(sampleRecipeCount) { upsert(recipe.copy(id = "recipe-$it", name = "Recipe $it")) }
  }
}

/**
 * Combines a dynamic list of flows into a single flow that emits the latest value of every source, preserving input order. Used by
 * [RecipeRepositoryImpl.observeRecipes] to reassemble all recipes whenever any recipe's graph changes.
 */
private fun <T> List<Flow<T>>.combineFlows(): Flow<List<T>> {
  if (isEmpty()) return flowOf(emptyList())
  val head = first()
  return drop(1).fold(head.map { listOf(it) }) { acc, next -> combine(acc, next) { list, v -> list + v } }
}
