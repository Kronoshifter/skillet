package com.kronos.skilletapp.data

import androidx.room3.withWriteTransaction
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.database.dao.EquipmentDao
import com.kronos.skilletapp.database.dao.IngredientDao
import com.kronos.skilletapp.database.dao.InstructionDao
import com.kronos.skilletapp.database.dao.RecipeDao
import com.kronos.skilletapp.model.Recipe
import com.kronos.skilletapp.model.RecipeSummary
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
