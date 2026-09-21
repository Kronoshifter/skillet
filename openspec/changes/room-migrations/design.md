## Context

The Room database (`RecipeDatabase`) is `version = 2` with a 6-table relational schema and **no** `autoMigrations` and **no** registered `Migration` objects. `SkilletApp.kt` opens it via `Room.databaseBuilder(...).fallbackToDestructiveMigration(true).addCallback(fkPragmaCallback).build()` and binds it as a Koin `createdAtStart()` single, so it opens at app launch. `room { schemaDirectory("$projectDir/schemas") }` is configured and both `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/1.json` and `2.json` are checked in.

The v1 → v2 jump (commit `1dc5e76`, part of `split-domain-entity-models`) was deliberately destructive: v1 was a single `recipe` table with `ingredients`/`instructions`/`equipment` as JSON blobs in TEXT columns; the v1 serializers (`RecipeConverters`, commit `6990790`) are deleted, so a data-preserving v1 → v2 is not recoverable in any case. No v1 database with real data survives.

Constraints: Room **2.8.4** (auto-migrations and the JVM `Room.testing()` path are both supported; no Room upgrade). The JVM unit-test gate already runs Kotest 6.2.3 on the JUnit platform (`app/build.gradle.kts`: `testOptions { unitTests.all { it.useJUnitPlatform() } }`, `tasks.withType<Test> { useJUnitPlatform() }`). **This change ships no schema bump** — the schema stays v2. See `openspec/speculation.md` for the full risk model and options matrix.

## Goals / Non-Goals

**Goals:**
- Make the migration chain **complete and unbroken** from version 1 to the current version 2, with the v1 → v2 step explicit.
- Remove the destructive fallback so any future gap **fails loudly** (launch crash) instead of silently wiping `recipes.db`.
- Declare `autoMigrations` so future resolvable bumps are auto-generated and future unresolvable bumps fail loudly.
- Add the project's **first migration tests** to the existing JVM Kotest gate, with a spike-first strategy for the one hard unknown (JVM native driver).
- Correct the two stale `AGENTS.md` schema claims.

**Non-Goals:**
- **No schema change** — version stays 2; no new/removed/renamed columns or tables; `app/schemas/` and `room.schemaLocation` are untouched.
- No Room version bump (2.8.4 has everything).
- No changes to entities, DAOs, repository, domain, ViewModels, or UI.
- No data-preserving v1 → v2 migration (impossible — v1 format deleted; accepted).
- No CI / pre-commit hooks (out of scope; correctness rests on locally-run tests).

## Decisions

**1. Test vehicle: JVM `Room.testing()` first, instrumented `MigrationTestHelper` as fallback — spike first (durable).**
The plan's only hard unknown is whether a pure-JVM SQLite driver resolves and its native library actually loads in this environment (no CI; local-only gate). Room 2.8.4 ships `room-common-jvm` / `room-migration-jvm`, and `androidx.sqlite:sqlite:2.7.1` is a KMP artifact whose `jvm` variant resolves to `sqlite-bundled-jvm` (a ~3.78 MB fat JAR embedding the PC SQLite native library). This makes a device-free path viable **in principle**.
- **Rationale (durable):** a JVM test runs in the existing `./gradlew test` Kotest gate with no device attached — a much stronger, more honest gate than an instrumented test that only runs when a device is plugged in. The human preference was "instrumented, *unless* Room provides device-free testing" — it does, so JVM is primary.
- **The spike is Task 1 and gates the rest:** add the driver dependency and write a minimal JVM test that builds the real `RecipeDatabase` via `Room.testing()`, inserts a row, closes, reopens, and asserts the row is intact. If the driver resolves and the native library loads, the JVM path is proven and the rest of the plan proceeds on JVM. If it fails (native lib won't load / driver won't resolve), the plan pivots to instrumented `MigrationTestHelper` tests (Task 1's acceptance criteria explicitly cover both outcomes).
- **Exact `Room.testing()` builder mechanics** (entry point, how the driver is supplied, `addMigrations` wiring) are confirmed by the spike and recorded in the test it produces — not assumed here.
- Alternative: instrumented `MigrationTestHelper` only. Rejected as primary — weaker gate (device-dependent), but retained as the documented fallback.
- Alternative: a third-party JVM SQLite (e.g. `org.xerial:sqlite-jdbc`). Rejected — `androidx.sqlite` is the driver Room itself is built against, so schema/behavior fidelity is highest.

**2. v1 → v2 is an explicit destructive custom `Migration(1, 2)` that recreates the v2 schema.**
A custom `Migration`'s `migrate(db)` is responsible for the **full** schema transformation (Room does not add tables after a custom migration). Since the step is destructive and produces an empty v2 schema, `migrate()` drops the v1 `recipe` table and creates all six v2 tables.
- **Rationale:** preserves today's exact behavior (the wipe), keeps the chain complete and resolvable from version 1, and documents intent. It is an honest record of the destructive jump that already shipped.
- **DDL is derived from the v2 entity definitions** and **validated against `2.json`** by the migration test (the post-migration schema must match the checked-in v2 schema). This bounds the hand-written-DDL risk.
- Alternative: an `AutoMigration(1, 2)`. Rejected — v1 → v2 (1 table → 6 tables, dropping the old JSON format) is not a cleanly auto-resolvable diff and is not honest about the destructiveness.
- Alternative: keep relying on the fallback for v1 → v2. Rejected — that is exactly the silent-wipe behavior this change removes.

**3. `autoMigrations` is declared on `@Database` (empty for now).**
`@Database(..., autoMigrations = [ ... ])` gains an `autoMigrations` list. It is **empty** in this change (no new schema version). Its presence means: future resolvable bumps are handled by generated `AutoMigration`s, and future unresolvable bumps hit the loud-failure path (no fallback) instead of a wipe.
- **Rationale:** the list is the registration point Room checks; an empty-but-present list is zero-cost now and is the load-bearing declaration for every future bump.
- **Constraint (spec-level, see delta spec):** `autoMigrations` computes *schema* diffs and is **blind to data format inside TEXT columns** (e.g. the serialized `Measurement` value). A converter-format change is a data migration, not a schema change — `autoMigrations` generates nothing for it. This is captured as a spec requirement so every future change inherits the warning.

**4. Remove `.fallbackToDestructiveMigration(true)`; register the migration on the builder.**
`SkilletApp.kt`'s `database(...)` becomes `Room.databaseBuilder(context, RecipeDatabase::class.java, "recipes.db").addMigrations(MIGRATION_1_2).addCallback(fkPragmaCallback).build()`. The `.fallbackToDestructiveMigration(true)` line is deleted.
- **Rationale:** with a complete chain (1 → 2 explicit, 2 → N auto), there is no legitimate gap to fall back over. Removing the fallback converts the next gap from silent data loss to a loud launch crash — the correct failure mode.
- **Ordering:** `addMigrations` and `addCallback` are both kept; the `fkPragmaCallback` stays in place (see Decision 7).

**5. Migration code home: `app/.../database/migrations/` (new package).**
`MIGRATION_1_2` lives in `com.kronos.skilletapp.database.migrations` (new `migrations/` directory). This is the going-forward home for per-version migrations.
- **Rationale:** colocates migrations with the `database/` package they belong to; a single obvious place for the next version bump.

**6. JVM driver dependency: `androidx.sqlite:sqlite:2.7.1` (test-only).**
Add a version catalog entry and a `testImplementation` dependency. `androidx.sqlite:sqlite` is KMP; in the `:app` module's local (JVM) unit-test configuration it resolves to the `jvm` variant → `sqlite-bundled-jvm` (fat JAR with the PC SQLite native library). Pin **2.7.1** (latest stable; `2.8.0-alpha01` is alpha and avoided).
- This is **independent of the Room version** (Room 2.8.4, SQLite driver 2.7.1) — the two are versioned separately.
- `androidx.room:room-testing` is added **only** if the spike forces the instrumented fallback (for `MigrationTestHelper`).
- **Rationale:** test-only scope keeps the production APK clean; the KMP resolution means one coordinate covers the JVM test classpath.

**7. FK pragma vs. migration ordering — invariant, not coupling (edge case 3).**
Room runs migrations with `foreign_keys` OFF; the app's `PRAGMA foreign_keys = ON` is set in `fkPragmaCallback.onOpen`, which fires **after** migrations, per connection. Auto-generated and the v1 → v2 DDL are unaffected. **Invariant:** the pragma is per-connection and set in `onOpen`; migration code SHALL NOT assume `foreign_keys` is ON and SHALL NOT couple to the pragma callback. (A future custom migration that moves data across FK-related tables may toggle the pragma itself.) No action in this change beyond keeping the callback registered and noting the invariant.

## Risks / Trade-offs

- **[JVM native driver won't resolve / load in this environment]** → Task 1 is a spike that proves it before any other work; the documented fallback is instrumented `MigrationTestHelper`. This is the plan's only hard unknown and it is isolated in the first task.
- **[Hand-written v2 DDL in `Migration(1,2)` drifts from the entities]** → the migration test asserts the post-migration schema matches the checked-in `2.json`, so drift fails the test rather than shipping.
- **[A missing future migration now crashes at launch (`createdAtStart`), i.e. a blank-screen app]** → that is the intended loud failure; the mitigation is the complete chain plus the v2-data-preservation test that opens the DB with existing data and asserts survival.
- **[Incidental schema drift during implementation silently becomes a migration]** → this change must not bump the version or alter the schema; any stray column/annotation change is a review blocker (edge case 4).
- **[`autoMigrations` "registered" is mistaken for "data migrations handled" for TEXT columns]** → captured as an explicit spec requirement (Decision 3 / delta spec) so the warning is inherited by future changes.
- **[Test DB file leaks into the repo / collides across tests]** → JVM tests use a temp/in-memory path per test (spike establishes the pattern); no checked-in `.db` files.

## Migration Plan

1. **Spike (de-risk):** add the `androidx.sqlite:sqlite` test dependency; write a minimal JVM Kotest test that opens the real `RecipeDatabase` via `Room.testing()`, inserts a row, closes, reopens, and asserts the row is intact. Confirm the driver resolves, the native library loads, and record the exact `Room.testing()` builder mechanics. **If it fails, pivot the remaining test tasks to instrumented `MigrationTestHelper` and stop here for human review.**
2. **Migration code:** add `database/migrations/` with `MIGRATION_1_2` (destructive: drop v1 `recipe`, recreate the six v2 tables; DDL from the entities, matching `2.json`).
3. **Wire it up:** add `autoMigrations` (empty) to `@Database` (version stays 2); in `SkilletApp.kt` add `.addMigrations(MIGRATION_1_2)` and **remove** `.fallbackToDestructiveMigration(true)`; keep `fkPragmaCallback`.
4. **Tests:** the v2-data-preservation test (from the spike, made permanent) + a v1 → v2 test (build a v1 DB, run `MIGRATION_1_2`, assert the result is an empty v2 schema matching `2.json`).
5. **Docs:** fix the two stale `AGENTS.md` schema claims.
6. **Gate:** `./gradlew :app:assembleDebug`, `./gradlew test`, `./gradlew :app:lint` all green; confirm no schema version bump and `app/schemas/` unchanged.

Rollback: revert the code. No user data is affected (no schema change; the dev device's v2 DB is untouched by an unchanged schema).

## Open Questions

- **None blocking.** The exact `Room.testing()` builder API and the precise mechanism for materializing a v1 DB in the v1 → v2 test are answered by the spike (Task 1) and the test task respectively, without changing the spec, the chosen approach, or the task breakdown.
