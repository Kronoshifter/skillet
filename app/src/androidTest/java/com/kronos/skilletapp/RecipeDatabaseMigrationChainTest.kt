package com.kronos.skilletapp

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.database.migrations.MIGRATION_1_2
import com.kronos.skilletapp.database.migrations.MIGRATION_2_3
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Full migration-chain test (bead `skillet-rm-09`, OpenSpec change `room3-migration` task 3.3).
 *
 * Proves the registered chain end to end on device: a v1 database (materialized from `1.json`) is driven through BOTH cuts in order —
 * MIGRATION_1_2 then MIGRATION_2_3, exactly the pair `SkilletApp.kt` registers via `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)` — with NO
 * exception at any step. Per cut, Room validates the resulting schema against the checked-in JSON for that version (2.json at cut 1, 3.json
 * at cut 2); reaching past each `runMigrationsAndValidate` call is itself that validation assertion.
 *
 * Both contracts are pinned:
 * - Cut 1 (1→2) is DESTRUCTIVE by design (design.md R8): MIGRATION_1_2 drops and recreates `recipe`, so the seeded v1 row must be GONE
 *   after this cut — not preserved. This test asserts that explicitly.
 * - Cut 2 (2→3) preserves data: a realistic v2-shaped dataset seeded into the post-cut-1 state (the same shape
 *   `RecipeDatabaseV3MigrationTest` uses, including two instruction_ingredient rows sharing instruction_id `ins-1` — exactly what a UNIQUE
 *   index would have rejected; all 8 indexes are non-unique per the F-1 resolution) must survive MIGRATION_2_3 intact, and all 8 new
 *   indexes must be present AND non-unique.
 *
 * Mechanics mirror `Room3MigrationSpikeTest` (room3-testing 3.0.0 API surface: `MigrationTestHelper(instrumentation, databaseClass, driver,
 * file)` with `driver` LAST; the sqlite-async extension is spelled `executeSQL`; scalar reads use prepare/step/getInt) and
 * `RecipeDatabaseV2DataTest` (fresh per-run file naming).
 */
@RunWith(AndroidJUnit4::class)
class RecipeDatabaseMigrationChainTest {

  private val instrumentation = InstrumentationRegistry.getInstrumentation()

  /** Fresh, uniquely-named file so re-runs never collide with prior state (rm-08 convention). */
  private val dbName = "v1-v3-chain-${System.currentTimeMillis()}"

  // Room 3 migration-test helper — exact constructor shape proven in Room3MigrationSpikeTest.
  // `driver` is LAST, mirroring SkilletApp.kt.
  @get:Rule
  val helper =
    MigrationTestHelper(
      instrumentation = instrumentation,
      databaseClass = RecipeDatabase::class,
      driver = AndroidSQLiteDriver(),
      file = instrumentation.targetContext.getDatabasePath(dbName),
    )

  // The 8 indexes MIGRATION_2_3 must create: (name as emitted in 3.json, table). All non-unique.
  private val expectedIndexes =
    listOf(
      "index_recipe_name" to "recipe",
      "index_ingredient_recipe_id" to "ingredient",
      "index_instruction_recipe_id" to "instruction",
      "index_equipment_recipe_id" to "equipment",
      "index_instruction_ingredient_instruction_id" to "instruction_ingredient",
      "index_instruction_ingredient_ingredient_id" to "instruction_ingredient",
      "index_instruction_equipment_instruction_id" to "instruction_equipment",
      "index_instruction_equipment_equipment_id" to "instruction_equipment",
    )

  @Test
  fun migrateV1ToV3_chainRunsNoExceptions_finalSchemaIsV3_v1RowGone_v2RowsSurvive() = runBlocking {
    // --- Materialize a v1 database from 1.json and seed a v1-shaped recipe row. ---
    val v1Connection = helper.createDatabase(1)
    seedV1(v1Connection)
    v1Connection.close()

    // --- Cut 1: apply MIGRATION_1_2; Room validates the resulting schema against 2.json. ---
    val afterCut1 = helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2))
    assertEquals("user_version must advance to 2", 2, afterCut1.queryInt("PRAGMA user_version"))

    // Destructive contract at the 1→2 step (design.md R8): the seeded v1 row is GONE. MIGRATION_1_2
    // drops and recreates `recipe`; nothing from v1 survives this cut by design.
    assertEquals(
      "the seeded v1 recipe row must be gone after MIGRATION_1_2 (destructive contract)",
      0,
      afterCut1.queryInt("SELECT COUNT(*) FROM recipe WHERE id = 'chain-v1'"),
    )
    assertEquals(
      "recipe table must be empty after MIGRATION_1_2",
      0,
      afterCut1.queryInt("SELECT COUNT(*) FROM recipe"),
    )

    // Seed a realistic v2-shaped dataset into the post-cut-1 state so the 2→3 data-preservation
    // guarantee is asserted on real rows, not vacuously.
    seedV2(afterCut1)
    afterCut1.close()

    // --- Cut 2: apply MIGRATION_2_3; Room validates the resulting schema against 3.json. Reaching
    // past this call is itself the "final schema == 3.json" assertion. ---

    helper.runMigrationsAndValidate(3, listOf(MIGRATION_2_3)).use { migrated ->
      assertEquals("user_version must advance to 3", 3, migrated.queryInt("PRAGMA user_version"))

      // Every row seeded at v2 survives the 2→3 cut.
      migrated.assertV2RowsIntact()

      // All 8 indexes exist under their 3.json names, none is unique, and no other user index
      // exists.
      migrated.assertIndexesPresentAndNonUnique()
    }
  }

  /**
   * Seed a v1-shaped recipe row against the only v1 table (`recipe`, per `1.json`): a single wide table whose ingredients / instructions /
   * equipment are JSON TEXT columns. All NOT NULL columns set; cover is NULL (nullable in v1).
   */
  private suspend fun seedV1(connection: SQLiteConnection) {
    connection.executeSQL(
      "INSERT INTO recipe (id, name, description, cover, notes, servings, ingredients, instructions, equipment, prep_time, cook_time, source_name, source_url) VALUES ('chain-v1', 'V1 Chain Test Recipe', 'A v1-shaped row seeded to pin the destructive 1->2 contract.', NULL, 'Seeded at schema version 1.', 4, '[{\"name\":\"flour\"}]', '[{\"text\":\"Mix.\"}]', '[{\"name\":\"bowl\"}]', 15, 30, 'Skillet Test Kitchen', 'https://example.com/v1-chain-test')"
    )
  }

  /**
   * Seed one realistic recipe across all six v2 tables, parent-before-child (FKs are CASCADE). The measurement column holds the exact JSON
   * the Room 3 [MeasurementConverters] converter emits. Same fixture shape as `RecipeDatabaseV3MigrationTest.seedV2`.
   */
  private suspend fun seedV2(connection: SQLiteConnection) {
    connection.executeSQL(
      "INSERT INTO recipe (id, name, description, cover, notes, servings, prep_time, cook_time, source_name, source_url) VALUES ('recipe-1', 'V3 Migration Test Recipe', 'A seeded recipe used to verify the v2 to v3 migration.', NULL, 'Do not edit this row; it is test seed data.', 4, 15, 30, 'Skillet Test Kitchen', 'https://example.com/v3-migration-test')"
    )
    connection.executeSQL(
      "INSERT INTO ingredient (id, recipe_id, position, name, measurement, raw, comment) VALUES ('ing-1', 'recipe-1', 1, 'flour', '{\"quantity\":250.0,\"unit\":{\"measurement_type\":\"Gram\"}}', '250 g flour', NULL)"
    )
    connection.executeSQL(
      "INSERT INTO ingredient (id, recipe_id, position, name, measurement, raw, comment) VALUES ('ing-2', 'recipe-1', 2, 'butter', '{\"quantity\":0.5,\"unit\":{\"measurement_type\":\"Cup\"}}', '1/2 cup butter', 'salted')"
    )
    connection.executeSQL(
      "INSERT INTO ingredient (id, recipe_id, position, name, measurement, raw, comment) VALUES ('ing-3', 'recipe-1', 3, 'fresh thyme', '{\"quantity\":1.0,\"unit\":{\"measurement_type\":\"Custom\",\"name\":\"sprig\"}}', '1 sprig fresh thyme', NULL)"
    )
    connection.executeSQL(
      "INSERT INTO instruction (id, recipe_id, position, text, image) VALUES ('ins-1', 'recipe-1', 1, 'Whisk the dry ingredients together.', NULL)"
    )
    connection.executeSQL(
      "INSERT INTO instruction (id, recipe_id, position, text, image) VALUES ('ins-2', 'recipe-1', 2, 'Fold in the butter and bake for 30 minutes.', NULL)"
    )
    connection.executeSQL("INSERT INTO equipment (id, recipe_id, position, name) VALUES ('eq-1', 'recipe-1', 1, 'mixing bowl')")
    connection.executeSQL("INSERT INTO equipment (id, recipe_id, position, name) VALUES ('eq-2', 'recipe-1', 2, 'oven')")
    // Join rows: instruction 1 uses flour + thyme (instruction_id `ins-1` appears twice —
    // realistic;
    // a UNIQUE index on the join columns would have rejected exactly this).
    connection.executeSQL(
      "INSERT INTO instruction_ingredient (instruction_id, ingredient_id, recipe_id, position) VALUES ('ins-1', 'ing-1', 'recipe-1', 1)"
    )
    connection.executeSQL(
      "INSERT INTO instruction_ingredient (instruction_id, ingredient_id, recipe_id, position) VALUES ('ins-1', 'ing-3', 'recipe-1', 2)"
    )
    connection.executeSQL(
      "INSERT INTO instruction_equipment (instruction_id, equipment_id, recipe_id, position) VALUES ('ins-2', 'eq-2', 'recipe-1', 1)"
    )
  }

  /** Per-table row counts + identity spot-checks on plain-text columns (getInt-only reads). */
  private fun SQLiteConnection.assertV2RowsIntact() {
    assertEquals("recipe row count", 1, this.queryInt("SELECT COUNT(*) FROM recipe"))
    assertEquals(
      "ingredient row count",
      3,
      this.queryInt("SELECT COUNT(*) FROM ingredient"),
    )
    assertEquals(
      "instruction row count",
      2,
      this.queryInt("SELECT COUNT(*) FROM instruction"),
    )
    assertEquals("equipment row count", 2, this.queryInt("SELECT COUNT(*) FROM equipment"))
    assertEquals(
      "instruction_ingredient row count",
      2,
      this.queryInt("SELECT COUNT(*) FROM instruction_ingredient"),
    )
    assertEquals(
      "instruction_equipment row count",
      1,
      this.queryInt("SELECT COUNT(*) FROM instruction_equipment"),
    )

    // Identity spot-checks via WHERE-equality counts (keeps reads on the proven getInt-only
    // surface).
    assertEquals(
      1,
      this.queryInt(
        "SELECT COUNT(*) FROM recipe WHERE id = 'recipe-1' AND name = 'V3 Migration Test Recipe' AND source_url = 'https://example.com/v3-migration-test'"
      ),
    )
    assertEquals(
      1,
      this.queryInt(
        "SELECT COUNT(*) FROM ingredient WHERE id = 'ing-2' AND name = 'butter' AND raw = '1/2 cup butter' AND comment = 'salted'"
      ),
    )
    assertEquals(
      1,
      this.queryInt("SELECT COUNT(*) FROM instruction WHERE id = 'ins-2' AND text = 'Fold in the butter and bake for 30 minutes.'"),
    )
    assertEquals(
      1,
      this.queryInt("SELECT COUNT(*) FROM equipment WHERE id = 'eq-1' AND name = 'mixing bowl'"),
    )
    assertEquals(
      1,
      this.queryInt("SELECT COUNT(*) FROM instruction_ingredient WHERE instruction_id = 'ins-1' AND ingredient_id = 'ing-3'"),
    )
    assertEquals(
      1,
      this.queryInt("SELECT COUNT(*) FROM instruction_equipment WHERE instruction_id = 'ins-2' AND equipment_id = 'eq-2'"),
    )
  }

  /**
   * Each of the 8 expected indexes exists under its 3.json name on the right table with a non-UNIQUE createSql, and no other user-created
   * index exists (auto-indexes are named `sqlite_autoindex_*`, so the NOT LIKE guard isolates user indexes).
   */
  private fun SQLiteConnection.assertIndexesPresentAndNonUnique() {
    for ((name, table) in expectedIndexes) {
      assertEquals(
        "index $name on $table must exist exactly once and be non-unique",
        1,
        queryInt(
          "SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' AND name = '$name' AND tbl_name = '$table' AND sql NOT LIKE '%UNIQUE%'"
        ),
      )
    }
    assertEquals(
      "exactly the 8 expected user indexes must exist",
      8,
      queryInt("SELECT COUNT(*) FROM sqlite_master WHERE type = 'index' AND name NOT LIKE 'sqlite_%'"),
    )
  }

  /** Single-int scalar read (the proven spike connection surface: prepare/step/getInt). */
  private fun SQLiteConnection.queryInt(sql: String): Int =
    prepare(sql).use {
      it.step()
      it.getInt(0)
    }
}
