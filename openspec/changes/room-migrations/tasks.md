## 1. Instrumented test vehicle — deps + smoke test (deferred to Room 3; spike closed per directive)

- [x] 1.1 Revert the two obsolete JVM-driver files — `gradle/libs.versions.toml` (remove the `androidx-sqlite = "2.7.1"` version entry and the `androidx-sqlite` library entry) and `app/build.gradle.kts` (remove `testImplementation(libs.androidx.sqlite)` from the Testing block) — then add a `room-testing` library entry to the catalog (`androidx.room:room-testing`, `version.ref = room`, i.e. 2.8.4 — no new version entry) and an `androidTestImplementation(libs.room.testing)` line in `app/build.gradle.kts`'s Testing block; verify `./gradlew :app:dependencies --configuration debugAndroidTestRuntimeClasspath` shows `androidx.room:room-testing:2.8.4` and `git diff` shows only those two intended files changed
- [x] 1.2 Write a minimal instrumented smoke test in `app/src/androidTest/` that (a) opens the real `RecipeDatabase` via `Room.databaseBuilder` on the instrumentation context, inserts a recipe row through the DAO, closes, reopens, and asserts the row is intact, and (b) constructs `androidx.room.testing.MigrationTestHelper` with `RecipeDatabase::class.java` and the checked-in schema directory (`app/schemas/com.kronos.skilletapp.database.RecipeDatabase/`), reading the v1 and v2 schema files; verify it passes on the connected device via `./gradlew :app:connectedDebugAndroidTest --tests "*<SmokeTest>*"` and record the exact `MigrationTestHelper` mechanics (schema-directory argument, `migrate(1)` usage) in the test — **DONE PER DIRECTIVE (2026-09-22): the spike (`skillet-scd`) is presumed successful per human directive and closed; the abandoned device run and its non-compiling scratch artifacts were discarded (removal tracked by cleanup bead `skillet-5tb`). The instrumented migration tests it de-risked are DEFERRED to the Room 3 migration (see 4.1/4.2 and the design.md Deferred work section); no test code from this task ships in this change.**

## 2. Migration code

- [x] 2.1 Create the `app/.../database/migrations/` package with `MIGRATION_1_2`, a destructive custom `Migration(1, 2)` that drops the v1 `recipe` table and recreates all six v2 tables (DDL derived from the v2 entities, matching `2.json`); verify it compiles and the six `CREATE TABLE` statements match the v2 entity definitions
- [x] 2.2 Verify `MIGRATION_1_2`'s DDL is complete (all six tables, foreign keys, indexes) against `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/2.json`

## 3. Wire up the database

- [x] 3.1 Add `autoMigrations` (empty list) to `@Database` in `RecipeDatabase.kt` while keeping `version = 2` (no schema change); verify the module compiles and the schema version is unchanged
- [x] 3.2 In `SkilletApp.kt`, add `.addMigrations(MIGRATION_1_2)` and remove the `.fallbackToDestructiveMigration(true)` call, keeping `fkPragmaCallback` registered; verify `./gradlew :app:assembleDebug` succeeds and the database opens at launch without the fallback

## 4. Migration tests (DEFERRED to Room 3)

- [ ] 4.1 Write the v2-data-preservation test as a plain instrumented Room test in `app/src/androidTest/`: open a v2 `RecipeDatabase` with existing rows (inserted via the DAO), close, reopen, and assert every row is intact — no migration runs since the version is unchanged, proving the fallback's removal is safe; verify it is in the permanent instrumented test set and passes via `./gradlew :app:connectedDebugAndroidTest` — **DEFERRED (2026-09-22, human scope decision): lands with the Room 3 migration (bead `skillet-m5r`, currently deferred). Bead `skillet-rm-08` stays deferred as the revival vehicle; the 2.8.4 `MigrationTestHelper` mechanics must be re-derived against the Room 3 API before implementation (design.md, Deferred work section).**
- [ ] 4.2 Write a v1 → v2 migration test in `app/src/androidTest/` using `androidx.room.testing.MigrationTestHelper`: construct the helper with `RecipeDatabase::class.java` and the checked-in schema directory (`app/schemas/com.kronos.skilletapp.database.RecipeDatabase/`), use `migrate(1)` to materialize a v1 database from schema `1.json` (seed a v1 recipe row), then open the DB so the registered `MIGRATION_1_2` runs; assert the resulting schema matches the checked-in v2 schema (`2.json`) and that the seeded v1 row is gone (destructive contract); verify it passes via `./gradlew :app:connectedDebugAndroidTest` — **DEFERRED (2026-09-22, human scope decision): lands with the Room 3 migration (bead `skillet-m5r`, currently deferred). Bead `skillet-rm-09` stays deferred as the revival vehicle; re-derive the `MigrationTestHelper` mechanics against the Room 3 API before implementation (design.md, Deferred work section).**

## 5. Docs

- [x] 5.1 Fix the two stale `AGENTS.md` schema claims (the "migrations not yet implemented / version 1 only" claim and the schemaLocation note); verify the corrected text matches reality (schema version 2, migrations registered)

## 6. Verification gates

- [ ] 6.1 Run `./gradlew :app:assembleDebug`, `./gradlew test`, and `./gradlew :app:lint`; verify all three are green (the JVM suite has no new DB tests but must stay green; `connectedDebugAndroidTest` is no longer part of the gate — the instrumented tests are deferred to Room 3)
- [ ] 6.2 Confirm no schema version bump and that `app/schemas/` is unchanged via `git status` / `git diff`; verify only the intended files (`SkilletApp.kt`, `RecipeDatabase.kt`, `database/migrations/*`, `AGENTS.md`, `app/build.gradle.kts`, `gradle/libs.versions.toml`) changed

## Bead mapping (1:1)

| Task | Bead | Status |
|------|------|--------|
| 1.1 | `skillet-azy` | closed |
| 1.2 | `skillet-scd` | closed (per human directive, 2026-09-22) |
| 2.1 | `skillet-rm-02` | open |
| 2.2 | `skillet-rm-06` | open |
| 3.1 | `skillet-rm-03` | open |
| 3.2 | `skillet-rm-07` | open |
| 4.1 | `skillet-rm-08` | **deferred** (Room 3 revival, with `skillet-m5r`) |
| 4.2 | `skillet-rm-09` | **deferred** (Room 3 revival, with `skillet-m5r`) |
| 5.1 | `skillet-rm-04` | open |
| 6.1 | `skillet-rm-10` | open |
| 6.2 | `skillet-rm-11` | open |

Unnumbered (not one of the tasks above): spike-artifact cleanup → `skillet-5tb` (open, P4 chore, discovered-from `skillet-scd`).
