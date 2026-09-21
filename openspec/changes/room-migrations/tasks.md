## 1. Spike — prove the JVM test vehicle (de-risk, gates the rest)

- [ ] 1.1 Add an `androidx-sqlite` (version 2.7.1) entry to `gradle/libs.versions.toml` and a `testImplementation` dependency in `app/build.gradle.kts`; verify it resolves on the `:app` JVM unit-test classpath (`./gradlew :app:dependencies --configuration debugUnitTestRuntimeClasspath` shows `androidx.sqlite:sqlite:2.7.1` → `sqlite-bundled-jvm`)
- [ ] 1.2 Write a minimal JVM Kotest test that builds the real `RecipeDatabase` via `Room.testing()`, inserts a recipe row through the DAO, closes, reopens, and asserts the row is intact; verify it passes via `./gradlew :app:testDebugUnitTest --tests "*<SpikeTest>*"` and record the exact `Room.testing()` builder mechanics in the test; **if the driver fails to resolve or its native library won't load, pivot the remaining test tasks to instrumented `MigrationTestHelper` and flag for human review**

## 2. Migration code

- [ ] 2.1 Create the `app/.../database/migrations/` package with `MIGRATION_1_2`, a destructive custom `Migration(1, 2)` that drops the v1 `recipe` table and recreates all six v2 tables (DDL derived from the v2 entities, matching `2.json`); verify it compiles and the six `CREATE TABLE` statements match the v2 entity definitions
- [ ] 2.2 Verify `MIGRATION_1_2`'s DDL is complete (all six tables, foreign keys, indexes) against `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/2.json`

## 3. Wire up the database

- [ ] 3.1 Add `autoMigrations` (empty list) to `@Database` in `RecipeDatabase.kt` while keeping `version = 2` (no schema change); verify the module compiles and the schema version is unchanged
- [ ] 3.2 In `SkilletApp.kt`, add `.addMigrations(MIGRATION_1_2)` and remove the `.fallbackToDestructiveMigration(true)` call, keeping `fkPragmaCallback` registered; verify `./gradlew :app:assembleDebug` succeeds and the database opens at launch without the fallback

## 4. Migration tests

- [ ] 4.1 Make the v2-data-preservation test permanent (a v2 DB with existing rows opens cleanly and every row survives, proving the fallback's removal is safe); verify it is in the permanent test set and passes via `./gradlew test`
- [ ] 4.2 Write a v1 → v2 test that builds a v1 database, applies `MIGRATION_1_2`, and asserts the result is an empty v2 schema matching `2.json` (JVM path if straightforward, else instrumented `MigrationTestHelper`); verify it passes

## 5. Docs

- [ ] 5.1 Fix the two stale `AGENTS.md` schema claims (the "migrations not yet implemented / version 1 only" claim and the schemaLocation note); verify the corrected text matches reality (schema version 2, migrations registered)

## 6. Verification gates

- [ ] 6.1 Run `./gradlew :app:assembleDebug`, `./gradlew test`, and `./gradlew :app:lint`; verify all three are green
- [ ] 6.2 Confirm no schema version bump and that `app/schemas/` is unchanged via `git status` / `git diff`; verify only the intended files (`SkilletApp.kt`, `RecipeDatabase.kt`, `database/migrations/*`, the new test(s), `build.gradle.kts`, `libs.versions.toml`, `AGENTS.md`) changed
