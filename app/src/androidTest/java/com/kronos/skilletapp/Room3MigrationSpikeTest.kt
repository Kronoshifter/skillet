package com.kronos.skilletapp

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.async.executeSQL
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kronos.skilletapp.database.RecipeDatabase
import com.kronos.skilletapp.database.migrations.MIGRATION_1_2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Room 3 migration-test mechanics spike (bead `skillet-g3-spike`).
 *
 * Purpose: re-derive, on-device and against the real room3-testing 3.0.0 artifact, the exact API surface that the deferred
 * data-preservation / all-migrations tests (`skillet-rm-08` / `skillet-rm-09`) were written against in Room 2.8.4
 * (androidx.room.testing.MigrationTestHelper). This file is intended to REMAIN in the tree as a standing instrumented smoke test: G3-GATE
 * runs the full connected suite, so it doubles as a regression guard that the migration-test machinery still compiles and executes on
 * device.
 *
 * Two parts: Part 1 (smoke) — open the current v2 [RecipeDatabase] with the platform driver on a fresh file. Part 2 (mechanics) — drive
 * MigrationTestHelper: createDatabase(1) -> seed -> runMigrationsAndValidate(2).
 */
@RunWith(AndroidJUnit4::class)
class Room3MigrationSpikeTest {

  private val instrumentation = InstrumentationRegistry.getInstrumentation()

  /** Fresh, uniquely-named files so re-runs never collide with prior state. */
  private val smokeDbName = "spike-smoke"
  private val migrationDbName = "spike-migration"

  // Part 2 helper — exact named-arg constructor shape from the Room 3 migration guide (targeting
  // room3-testing 3.0.3; compiled here against 3.0.0). `driver` is LAST, mirroring SkilletApp.kt.
  @get:Rule
  val helper =
    MigrationTestHelper(
      instrumentation = instrumentation,
      databaseClass = RecipeDatabase::class,
      driver = AndroidSQLiteDriver(),
      file = instrumentation.targetContext.getDatabasePath(migrationDbName),
    )

  // Part 1 — bead-required smoke: open the v2 RecipeDatabase with the platform driver on a fresh,
  // uniquely-named file. Builder mirrors SkilletApp.kt essentials (coroutine context + driver LAST),
  // minus migrations/callbacks, since this is a fresh create at the current (@Database) version.
  @Test
  fun smoke_openV2RecipeDatabase_withPlatformDriver() {
    val context = instrumentation.targetContext
    val dbFile = context.getDatabasePath(smokeDbName)
    if (dbFile.exists()) dbFile.delete() // ensure a fresh file for this run

    val db =
      Room.databaseBuilder(
          context = context,
          klass = RecipeDatabase::class.java,
          name = smokeDbName,
        )
        .setQueryCoroutineContext(Dispatchers.IO)
        .setDriver(AndroidSQLiteDriver())
        .build()

    // Force a real open (materializes the file) by running a trivial query through the DAO; on a
    // fresh v2 database this returns an empty list. build() throwing would already fail the test.
    val recipes = runBlocking { db.recipeDao().getAll() }
    db.close()

    assertTrue(recipes.isEmpty())
    // The platform (file) driver must have materialized the database file on disk.
    assertTrue(dbFile.exists())
  }

  // Part 2 — mechanics derivation: the Room 3 equivalent of Room 2's helper.migrate(n).
  // createDatabase(1) builds the v1 schema; seed a row against the only v1 table (`recipe`);
  // runMigrationsAndValidate(2, [MIGRATION_1_2]) applies our destructive migration and returns a
  // usable connection. MIGRATION_1_2 is destructive (drops + recreates `recipe`), so we assert the
  // migration RAN and the returned connection is usable — NOT that the seeded row survived.
  @Test
  fun migrate1To2_runsAndReturnsUsableConnection() = runBlocking {
    // Room 3 equivalent of Room 2's helper.migrate(1): create the earliest-version database.
    val connection = helper.createDatabase(1)

    // Seed a valid v1 row against `recipe` (the only table in 1.json). All NOT NULL columns set.
    // NOTE: the 3.0.0 sqlite-async extension is named `executeSQL` (matches our Migrations.kt);
    // the guide's `execSQL` spelling does not resolve on this classpath.
    connection.executeSQL(
      "INSERT INTO recipe (id, name, description, notes, servings, ingredients, instructions, equipment, prep_time, cook_time, source_name, source_url) VALUES ('spike-1', 'Spike Recipe', '', '', 1, '', '', '', 0, 0, '', '')"
    )
    connection.close()

    // Apply the v1 -> v2 migration; Room validates the resulting schema against 2.json.
    val migratedConnection = helper.runMigrationsAndValidate(2, listOf(MIGRATION_1_2))

    // Usable + advanced: post-migration user_version must be 2. (Data survival is out of scope —
    // MIGRATION_1_2 is destructive; that assertion belongs to rm-08/rm-09.)
    val userVersion =
      migratedConnection.prepare("PRAGMA user_version").use {
        it.step()
        it.getInt(0)
      }

    // Reaching here (no throw from runMigrationsAndValidate) + user_version == 2 proves the
    // migration RAN, Room validated the resulting schema against 2.json, and the connection is usable.
    assertEquals(2, userVersion)

    migratedConnection.close()
  }
}
