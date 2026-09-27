package com.kronos.skilletapp

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kronos.measurement.model.Measurement
import com.kronos.measurement.model.MeasurementUnit
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.database.entity.EquipmentEntity
import com.kronos.skilletapp.database.entity.IngredientEntity
import com.kronos.skilletapp.database.entity.InstructionEntity
import com.kronos.skilletapp.database.entity.InstructionEquipmentEntity
import com.kronos.skilletapp.database.entity.InstructionIngredientEntity
import com.kronos.skilletapp.database.entity.RecipeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Permanent v2 data-preservation test (bead `skillet-rm-08`, OpenSpec change `room3-migration` task 4.1).
 *
 * Proves the `fallbackToDestructiveMigration` removal is safe: opening an EXISTING v2 [RecipeDatabase] (one already populated, at the
 * current `@Database` version 2) does NOT run any migration and does NOT wipe data — every row survives a genuine close -> reopen of the
 * same file.
 *
 * Mechanics (re-derived on-device in `skillet-g3-spike`, mirrored here):
 * - JUnit4 + [AndroidJUnit4] + [InstrumentationRegistry]; coroutine wrapper is `runBlocking` (no coroutines-test in the catalog).
 * - Builder mirrors `SkilletApp.kt`: `.setQueryCoroutineContext(Dispatchers.IO)` then `.setDriver(...)` LAST, with the platform driver
 *   [AndroidSQLiteDriver] (Room 3 bundles no SQLite driver).
 * - This is a FRESH create at the current version, so no `.addMigrations` / callback are needed.
 */
@RunWith(AndroidJUnit4::class)
class RecipeDatabaseV2DataTest {

  private val instrumentation = InstrumentationRegistry.getInstrumentation()

  /** Fresh, uniquely-named file so re-runs never collide with prior state (same convention as the spike). */
  private val dbName = "v2-preservation-${System.currentTimeMillis()}"

  // --- Seeded fixture: one realistic multi-table recipe spanning all six v2 tables. ---

  private val recipeId = "recipe-1"
  private val recipe =
    RecipeEntity(
      id = recipeId,
      name = "V2 Preservation Test Recipe",
      description = "A seeded recipe used to verify v2 data preservation across close/reopen.",
      cover = null,
      notes = "Do not edit this row; it is test seed data.",
      servings = 4,
      prepTime = 15,
      cookTime = 30,
      sourceName = "Skillet Test Kitchen",
      sourceUrl = "https://example.com/v2-preservation-test",
    )

  // Three ingredients exercising the JSON column-type converters: two named data-object units and one
  // Custom unit (the non-singleton discriminator path in MeasurementConverters).
  private val flourMeasurement = Measurement(250f, MeasurementUnit.Gram)
  private val butterMeasurement = Measurement(0.5f, MeasurementUnit.Cup)
  private val thymeMeasurement = Measurement(1f, MeasurementUnit.Custom("sprig"))

  private val ingredients =
    listOf(
      IngredientEntity(
        id = "ing-1",
        recipeId = recipeId,
        position = 1,
        name = "flour",
        measurement = flourMeasurement,
        raw = "250 g flour",
        comment = null,
      ),
      IngredientEntity(
        id = "ing-2",
        recipeId = recipeId,
        position = 2,
        name = "butter",
        measurement = butterMeasurement,
        raw = "1/2 cup butter",
        comment = "salted",
      ),
      IngredientEntity(
        id = "ing-3",
        recipeId = recipeId,
        position = 3,
        name = "fresh thyme",
        measurement = thymeMeasurement,
        raw = "1 sprig fresh thyme",
        comment = null,
      ),
    )

  private val instructions =
    listOf(
      InstructionEntity(id = "ins-1", recipeId = recipeId, position = 1, text = "Whisk the dry ingredients together."),
      InstructionEntity(id = "ins-2", recipeId = recipeId, position = 2, text = "Fold in the butter and bake for 30 minutes."),
    )

  private val equipment =
    listOf(
      EquipmentEntity(id = "eq-1", recipeId = recipeId, position = 1, name = "mixing bowl"),
      EquipmentEntity(id = "eq-2", recipeId = recipeId, position = 2, name = "oven"),
    )

  // Join rows: instruction 1 uses flour + thyme; instruction 2 uses the oven.
  private val instructionIngredients =
    listOf(
      InstructionIngredientEntity(instructionId = "ins-1", ingredientId = "ing-1", recipeId = recipeId, position = 1),
      InstructionIngredientEntity(instructionId = "ins-1", ingredientId = "ing-3", recipeId = recipeId, position = 2),
    )

  private val instructionEquipment =
    listOf(InstructionEquipmentEntity(instructionId = "ins-2", equipmentId = "eq-2", recipeId = recipeId, position = 1))

  @Test
  fun v2DataPreservedAcrossCloseAndReopen() {
    val context = instrumentation.targetContext
    val dbFile = context.getDatabasePath(dbName)
    if (dbFile.exists()) dbFile.delete() // ensure a fresh file for this run

    // --- Open: fresh create at the current (@Database) version 2. No migrations/callbacks needed. ---
    val db = buildDatabase(context, dbName)

    // --- Seed through the DAOs, parent-before-child per the FK (CASCADE) relations. ---
    seed(db)

    // Sanity: every seeded table is populated before we close.
    assertEquals(1, runBlocking { db.recipeDao().getAll() }.size)
    assertEquals(ingredients.size, runBlocking { db.ingredientDao().getByRecipeId(recipeId) }.size)
    assertEquals(instructions.size, runBlocking { db.instructionDao().getByRecipeId(recipeId) }.size)
    assertEquals(equipment.size, runBlocking { db.equipmentDao().getByRecipeId(recipeId) }.size)

    // --- Close the instance and prove it is genuinely closed (a closed Room DB rejects queries). ---
    db.close()
    val closedRejectsQueries =
      try {
        runBlocking { db.recipeDao().getAll() }
        false // if a query still succeeded, the instance was not actually closed
      } catch (e: Exception) {
        true // threw -> the underlying connection is gone; the instance is really closed
      }
    assertTrue(
      "expected the closed RecipeDatabase to reject a DAO query (genuine close), but it served one",
      closedRejectsQueries,
    )

    // --- Reopen the SAME file with identical builder config. assertNotSame guards against Room handing
    // back the cached (closed) instance; if that ever happens on this platform/Room version, force a fresh
    // instance via .allowInvalidated(true) on the reopen builder. ---
    val reopened = buildDatabase(context, dbName)
    assertNotSame("reopen must produce a distinct instance, not the closed one", db, reopened)

    // --- Assert every seeded row is still there: per-table counts + identity spot-checks, including a
    // measurement round-trip through the JSON column-type converters. Fresh queries on the new instance. ---
    assertRecipeIntact(reopened)
    assertIngredientsIntact(reopened)
    assertInstructionsIntact(reopened)
    assertEquipmentIntact(reopened)
    assertJoinTablesIntact(reopened)

    reopened.close()
  }

  private fun buildDatabase(context: android.content.Context, name: String): RecipeDatabase =
    Room.databaseBuilder(
        context = context,
        klass = RecipeDatabase::class.java,
        name = name,
      )
      .setQueryCoroutineContext(Dispatchers.IO)
      .setDriver(AndroidSQLiteDriver())
      .build()

  /** Insert one realistic recipe across all six v2 tables, parent-before-child (FKs are CASCADE). */
  private fun seed(db: RecipeDatabase) {
    runBlocking {
      db.recipeDao().upsert(recipe) // root
      db.ingredientDao().upsertAll(ingredients) // children of recipe
      db.instructionDao().upsertInstructions(instructions) // children of recipe
      db.equipmentDao().upsertAll(equipment) // children of recipe
      db.instructionDao().upsertInstructionIngredients(instructionIngredients) // join: instruction + ingredient
      db.instructionDao().upsertInstructionEquipment(instructionEquipment) // join: instruction + equipment
    }
  }

  private fun assertRecipeIntact(db: RecipeDatabase) {
    val recipes = runBlocking { db.recipeDao().getAll() }
    assertEquals("recipe row count", 1, recipes.size)
    val stored = recipes.single()
    assertEquals(recipe.id, stored.id)
    assertEquals(recipe.name, stored.name)
    assertEquals(recipe.sourceUrl, stored.sourceUrl)
    assertEquals(recipe.servings, stored.servings)
    assertEquals(recipe.prepTime, stored.prepTime)
    assertEquals(recipe.cookTime, stored.cookTime)
  }

  private fun assertIngredientsIntact(db: RecipeDatabase) {
    val stored = runBlocking { db.ingredientDao().getByRecipeId(recipeId) }
    assertEquals("ingredient row count", ingredients.size, stored.size)

    // Identity + measurement round-trip through the JSON column-type converters (named + Custom units).
    val byId = stored.associateBy { it.id }
    for (expected in ingredients) {
      val actual = checkNotNull(byId[expected.id]) { "missing ingredient ${expected.id}" }
      assertEquals(expected.name, actual.name)
      assertEquals(
        "measurement round-trip failed for ${expected.id}",
        expected.measurement,
        actual.measurement,
      )
      assertEquals(expected.raw, actual.raw)
    }
    // Explicit spot-checks required by the spec.
    val flour = byId["ing-1"]!!
    assertEquals("flour", flour.name)
    assertEquals(flourMeasurement, flour.measurement)
  }

  private fun assertInstructionsIntact(db: RecipeDatabase) {
    val stored = runBlocking { db.instructionDao().getByRecipeId(recipeId) }
    assertEquals("instruction row count", instructions.size, stored.size)
    val byId = stored.associateBy { it.id }
    for (expected in instructions) {
      val actual = checkNotNull(byId[expected.id]) { "missing instruction ${expected.id}" }
      assertEquals(expected.text, actual.text)
    }
  }

  private fun assertEquipmentIntact(db: RecipeDatabase) {
    val stored = runBlocking { db.equipmentDao().getByRecipeId(recipeId) }
    assertEquals("equipment row count", equipment.size, stored.size)
    val byId = stored.associateBy { it.id }
    for (expected in equipment) {
      val actual = checkNotNull(byId[expected.id]) { "missing equipment ${expected.id}" }
      assertEquals(expected.name, actual.name)
    }
  }

  private fun assertJoinTablesIntact(db: RecipeDatabase) {
    val storedIng = runBlocking { db.instructionDao().selectInstructionIngredients(recipeId) }
    assertEquals("instruction_ingredient row count", instructionIngredients.size, storedIng.size)
    for (expected in instructionIngredients) {
      assertTrue(
        "missing join row ${expected.instructionId}->${expected.ingredientId}",
        storedIng.any { it.instructionId == expected.instructionId && it.ingredientId == expected.ingredientId },
      )
    }

    val storedEq = runBlocking { db.instructionDao().selectInstructionEquipment(recipeId) }
    assertEquals("instruction_equipment row count", instructionEquipment.size, storedEq.size)
    for (expected in instructionEquipment) {
      assertTrue(
        "missing join row ${expected.instructionId}->${expected.equipmentId}",
        storedEq.any { it.instructionId == expected.instructionId && it.equipmentId == expected.equipmentId },
      )
    }
  }
}
