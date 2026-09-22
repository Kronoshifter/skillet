## Context

The Room database (`RecipeDatabase`) is `version = 2` with a 6-table relational schema and **no** `autoMigrations` and **no** registered `Migration` objects. `SkilletApp.kt` opens it via `Room.databaseBuilder(...).fallbackToDestructiveMigration(true).addCallback(fkPragmaCallback).build()` and binds it as a Koin `createdAtStart()` single, so it opens at app launch. `room { schemaDirectory("$projectDir/schemas") }` is configured and both `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/1.json` and `2.json` are checked in.

The v1 → v2 jump (commit `1dc5e76`, part of `split-domain-entity-models`) was deliberately destructive: v1 was a single `recipe` table with `ingredients`/`instructions`/`equipment` as JSON blobs in TEXT columns; the v1 serializers (`RecipeConverters`, commit `6990790`) are deleted, so a data-preserving v1 → v2 is not recoverable in any case. No v1 database with real data survives.

Constraints: Room **2.8.4** (auto-migrations and the instrumented `androidx.room:room-testing` / `MigrationTestHelper` path are both supported; no Room upgrade). **This change ships no schema bump** — the schema stays v2. A device/emulator IS available, but per human directive (2026-09-22) the instrumented migration tests are **deferred to the Room 3 migration** (bead `skillet-m5r`); the final gate therefore does **not** run `connectedDebugAndroidTest` (see the Deferred work section).

## Goals / Non-Goals

**Goals:**
- Make the migration chain **complete and unbroken** from version 1 to the current version 2, with the v1 → v2 step explicit.
- Remove the destructive fallback so any future gap **fails loudly** (launch crash) instead of silently wiping `recipes.db`.
- Declare `autoMigrations` so future resolvable bumps are auto-generated and future unresolvable bumps fail loudly.
- **Instrumented migration tests are deferred to the Room 3 migration** (bead `skillet-m5r`) per human directive (2026-09-22) — no `androidTest` code ships in this change; see the Deferred work section for the revival handoff.
- Correct the two stale `AGENTS.md` schema claims.

**Non-Goals:**
- **No schema change** — version stays 2; no new/removed/renamed columns or tables; `app/schemas/` and `room.schemaLocation` are untouched.
- No Room version bump (2.8.4 has everything).
- No changes to entities, DAOs, repository, domain, ViewModels, or UI.
- No data-preserving v1 → v2 migration (impossible — v1 format deleted; accepted).
- **No JVM unit-test database vehicle** — a JVM-loadable SQLite driver is unreachable from `:app` local unit tests in this environment (see Decision 1, pivot evidence). Extracting the Room database into a pure-Kotlin module to enable a JVM path is a **future optimization only, explicitly out of scope** — it intersects the in-flight `split-domain-entity-models` change.
- **No instrumented tests in this change** — deferred to the Room 3 migration (see Decision 8 and the Deferred work section).
- No CI / pre-commit hooks (out of scope; correctness in this change rests on the locally-run `assembleDebug` + `test` + `lint` gate).

## Decisions

**1. Test vehicle: instrumented `MigrationTestHelper` (on-device) first — spike first (durable; supersedes the original JVM-first decision).**

> **Scope update (2026-09-22, human directive):** this vehicle is **deferred to the Room 3 migration** (bead `skillet-m5r`). The spike (`skillet-scd`) closed presumed-successful per human directive; its abandoned scratch artifacts were discarded (removal tracked by cleanup bead `skillet-5tb`). The `room-testing:2.8.4` androidTest dependency **stays** pre-staged. See Decision 8 and the Deferred work section. The JVM-dead-end evidence below remains durable.

The original plan made a pure-JVM vehicle primary (`Room.testing()` + the KMP `androidx.sqlite:sqlite` driver's `jvm` variant on `:app`'s unit-test classpath, with `MigrationTestHelper` as the documented fallback). That premise was **definitively disproven** by a 3-round verification (evidence recorded on `skillet-rm-01`):

1. **Round 1** — plain `testImplementation(libs.androidx.sqlite)` (2.7.1) resolves on `debugUnitTestRuntimeClasspath` to the **android** variant (`sqlite-android`), not the JVM one: AGP 9.3.1's unit-test classpath requests `org.gradle.jvm.environment=android`.
2. **Round 2** — forcing the `jvm` variant via `JvmAttributes.JVM_ENVIRONMENT_ATTRIBUTE` fails at Kotlin-DSL script compile: `org.gradle.api.attributes.JvmAttributes` is not on Gradle 9's `.kts` script-compile classpath (implementation jar, not `-api`).
3. **Round 3** — spelling the attribute as `Attribute.of("org.gradle.jvm.environment", String::class.java), "jvm"` **compiles** but resolution fails `No matching variant`: the consumer requires BOTH `jvm.environment=jvm` (forced) AND `kotlin.platform.type=androidJvm` (AGP-pinned); the KMP artifact's `jvm` variant offers `platform.type=jvm` only, its android variant offers `env=android` only. No variant satisfies both.

**Conclusion: a JVM-loadable SQLite driver is unreachable from `:app` local unit tests in this environment.** The plan therefore pivots (human-authorized):

- **Primary vehicle: instrumented `androidx.room:room-testing` (`MigrationTestHelper`) tests** in `app/src/androidTest/`, driven by the checked-in schema JSONs in `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/` (v1 and `2.json`). `MigrationTestHelper` materializes real databases from those JSON files and runs the app's **registered** `Migration` objects against them — exactly the v1 → v2 contract this change introduces. *(Deferred for this change — see the Deferred work section.)*
- **Rationale (durable):** a device/emulator is available, so the instrumented vehicle is fully executable here. It exercises the real on-device path (real `Room.databaseBuilder`, real registered migrations, real checked-in schema files) — higher fidelity than a JVM driver shim would have been. *(The original final gate ran `./gradlew :app:connectedDebugAndroidTest`; that portion is deferred with the vehicle.)*
- **The spike was Task 1.2 and gated the test tasks:** an **instrumented smoke test** proves (a) the real `RecipeDatabase` opens on-device via `Room.databaseBuilder` and a row survives close/reopen, and (b) `MigrationTestHelper` can be constructed and can read the v1/v2 schema files from the checked-in schema directory. If the smoke test fails, the plan stops for human review — everything downstream assumes this vehicle works. *(Deferred: the spike closed 2026-09-22 presumed-successful per human directive.)*
- **Exact `MigrationTestHelper` API mechanics** (constructor arguments, the schema-directory argument, how `migrate(1)` materializes a v1 DB) were being confirmed by the spike; the partial 2.8.4 learnings are preserved in the Deferred work section and must be re-derived against the Room 3 API.
- **Future optimization (out of scope):** a JVM vehicle becomes viable if the Room database is extracted into a pure-Kotlin module (the only way off the AGP-pinned unit-test classpath). That refactor intersects the in-flight `split-domain-entity-models` change and is explicitly deferred — noted here so the next planner does not re-litigate the pivot.
- Alternative (historical): JVM `Room.testing()` first. Rejected — see the 3-round evidence above; the premise is false in this environment, not merely risky.
- Alternative: a third-party JVM SQLite (e.g. `org.xerial:sqlite-jdbc`). Rejected for the same reason — any JVM driver on the `:app` unit-test classpath is blocked by the AGP platform-attribute pin, and it would also lose schema/behavior fidelity vs. Room's own driver.

**2. v1 → v2 is an explicit destructive custom `Migration(1, 2)` that recreates the v2 schema.**
A custom `Migration`'s `migrate(db)` is responsible for the **full** schema transformation (Room does not add tables after a custom migration). Since the step is destructive and produces an empty v2 schema, `migrate()` drops the v1 `recipe` table and creates all six v2 tables.
- **Rationale:** preserves today's exact behavior (the wipe), keeps the chain complete and resolvable from version 1, and documents intent. It is an honest record of the destructive jump that already shipped.
- **DDL is derived from the v2 entity definitions** and **validated against `2.json`** by Task 2.2 before merge (the instrumented schema-comparison check returns with the Room 3 revival). This bounds the hand-written-DDL risk.
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

**6. Test dependency: `androidx.room:room-testing:2.8.4` (androidTest-only).**
Add a `room-testing` library entry to `gradle/libs.versions.toml` (`androidx.room:room-testing`, `version.ref = room` — i.e. the existing `room` 2.8.4 version entry; no new version entry) and an `androidTestImplementation(libs.room.testing)` line in `app/build.gradle.kts`.
- **Rationale:** androidTest scope keeps the production APK clean; `room-testing` is the artifact Room itself ships for `MigrationTestHelper`, and pinning it to the same 2.8.4 as `room-runtime` guarantees schema/behavior fidelity.
- **The obsolete Round-1 edits are reverted as part of this task:** the uncommitted `androidx-sqlite = "2.7.1"` version entry + `androidx-sqlite` library entry in `gradle/libs.versions.toml` and `testImplementation(libs.androidx.sqlite)` in `app/build.gradle.kts` come out with the JVM driver. The earlier suggested commit message for those edits ("build(app): add androidx.sqlite 2.7.1 test driver dependency") is dead — do not reuse it.
- **Kept after the deferral (2026-09-22):** this dependency has already landed (bead `skillet-azy` closed) and **stays in the build**, pre-staged for the Room 3 revival (see Decision 8). Nothing reverts the dirty `app/build.gradle.kts` / `gradle/libs.versions.toml` working-tree changes.

**7. FK pragma vs. migration ordering — invariant, not coupling (edge case 3).**
Room runs migrations with `foreign_keys` OFF; the app's `PRAGMA foreign_keys = ON` is set in `fkPragmaCallback.onOpen`, which fires **after** migrations, per connection. Auto-generated and the v1 → v2 DDL are unaffected. **Invariant:** the pragma is per-connection and set in `onOpen`; migration code SHALL NOT assume `foreign_keys` is ON and SHALL NOT couple to the pragma callback. (A future custom migration that moves data across FK-related tables may toggle the pragma itself.) No action in this change beyond keeping the callback registered and noting the invariant.

**8. Instrumented migration tests are deferred to the Room 3 migration (bead `skillet-m5r`) — human directive, 2026-09-22.**
Per the human scope decision, no instrumented tests ship in this change:
- The smoke spike (`skillet-scd`) is **presumed successful per human directive** and closed as done. Its abandoned scratch artifacts — an untracked, non-compiling `RoomInstrumentedSmokeTest.kt` and a redundant untracked `app/src/androidTest/assets/` copy of the schema JSONs — are removed by dedicated cleanup bead `skillet-5tb`; the non-compiling test would otherwise break any future `connectedDebugAndroidTest` compile.
- The two instrumented test tasks — v2-data-preservation (`skillet-rm-08`) and the v1 → v2 `MigrationTestHelper` test (`skillet-rm-09`) — are **deferred, not closed**, as the Room 3 revival vehicle; they land with `skillet-m5r` (itself deferred, unchanged).
- **The `androidx.room:room-testing:2.8.4` androidTest dependency is KEPT** — pre-staged for the Room 3 revival (see Decision 6).
- **The verification gate shrinks accordingly** (Task 6.1): `./gradlew :app:assembleDebug`, `./gradlew test`, `./gradlew :app:lint`. `connectedDebugAndroidTest` drops out of the gate because no instrumented tests ship in this change (the pre-existing trivial `ExampleInstrumentedTest.kt` is out of scope and no longer run by the gate).
- **Safety of the fallback removal without the instrumented tests is by construction:** the only registered migration is v1 → v2 and it is destructive; the migration is v1 → v2 only, so existing v2 user data is untouched; and no unhandled v2 → v3 path exists yet. The data-preservation requirement in the delta spec therefore remains true by construction until the Room 3 revival adds the automated check.

## Deferred work: instrumented migration tests (revive with Room 3 / `skillet-m5r`)

The instrumented test vehicle (Decisions 1 and 8) is deferred to the Room 3 migration. Revival vehicles: beads `skillet-rm-08` (v2-data-preservation test) and `skillet-rm-09` (v1 → v2 `MigrationTestHelper` test), both deferred, landing with `skillet-m5r`. The `androidx.room:room-testing:2.8.4` androidTest dependency **stays** in the build, pre-staged for the revival.

**Verified learnings from the abandoned 2.8.4 spike (preserved so the revival is cheap):**
1. In Room 2.8.4, the `MigrationTestHelper` constructors take `Instrumentation` **first, not `Context`** — `(Instrumentation, Class<out RoomDatabase>)` or `(Instrumentation, String assetsFolder, SupportSQLiteOpenHelper.Factory)`.
2. `createDatabase(name, version)` returns an **open `SupportSQLiteDatabase`**, not a `File` — close it in a `finally` block; there is no `File.delete` cleanup.
3. Schema JSONs reach the test APK's assets at `schemas/com.kronos.skilletapp.database.RecipeDatabase/<version>.json` via the Room Gradle plugin's `copyRoomSchemasToAndroidTestAssets*` tasks. (A manual copy under `app/src/androidTest/assets/` was also attempted — redundant; the plugin task is the canonical path.)
4. `migrate(...)`'s exact 2.8.4 signature was **not yet verified** — re-derive it (e.g. `javap` on the `room-testing` classes.jar in the Gradle cache) before writing any test against it.
5. `--tests` is **not supported** on `connectedDebugAndroidTest` (unit-test-only option) — the full task runs all androidTest classes.

**API warning:** items 1–5 are 2.8.4-specific. The `MigrationTestHelper` API changed between 2.8.4 and Room 3 — re-derive all mechanics against the Room 3 API before implementing `skillet-rm-08` / `skillet-rm-09`.

## Risks / Trade-offs

- **[Device/emulator availability no longer affects this change]** → the gate is now `assembleDebug` + `test` + `lint` only (instrumented tests deferred to Room 3); device availability is a question again at the `skillet-m5r` revival, not for this change.
- **[Hand-written v2 DDL in `Migration(1,2)` drifts from the entities]** → Task 2.2 (`skillet-rm-06`) verifies the DDL is complete against the checked-in `2.json` before merge; the automated `MigrationTestHelper` schema comparison returns with the Room 3 revival (`skillet-rm-09`).
- **[A missing future migration now crashes at launch (`createdAtStart`), i.e. a blank-screen app]** → that is the intended loud failure; the mitigation is the complete chain plus the by-construction argument (the only registered step is v1 → v2 and is destructive; v2 user data is untouched; no unhandled v2 → v3 path exists yet). The automated v2-data-preservation check returns with the Room 3 revival (`skillet-rm-08`).
- **[Incidental schema drift during implementation silently becomes a migration]** → this change must not bump the version or alter the schema; any stray column/annotation change is a review blocker (edge case 4).
- **[`autoMigrations` "registered" is mistaken for "data migrations handled" for TEXT columns]** → captured as an explicit spec requirement (Decision 3 / delta spec) so the warning is inherited by future changes.
- **[Instrumented tests pollute or destroy the real on-device `recipes.db` — applies at the Room 3 revival]** → when revived, instrumented tests operate on test-harness databases (scratch files managed by `MigrationTestHelper`, or per-test databases cleaned up via `context.deleteDatabase(...)`); no checked-in `.db` files, and the app's `recipes.db` is never the test target.

## Migration Plan

1. **Instrumented deps + smoke test (de-risk) — closed:** the obsolete JVM-driver edits were reverted and the `room-testing` catalog entry (`version.ref = room`, 2.8.4) + `androidTestImplementation` landed (bead `skillet-azy`, closed). The instrumented smoke spike (bead `skillet-scd`) closed 2026-09-22 presumed-successful per human directive; its untracked scratch artifacts are removed by cleanup bead `skillet-5tb`. The instrumented test tasks themselves are **deferred to Room 3** (`skillet-m5r`) — see the Deferred work section.
2. **Migration code:** add `database/migrations/` with `MIGRATION_1_2` (destructive: drop v1 `recipe`, recreate the six v2 tables; DDL from the entities, matching `2.json`).
3. **Wire it up:** add `autoMigrations` (empty) to `@Database` (version stays 2); in `SkilletApp.kt` add `.addMigrations(MIGRATION_1_2)` and **remove** `.fallbackToDestructiveMigration(true)`; keep `fkPragmaCallback`.
4. **Tests — DEFERRED to Room 3 (`skillet-m5r`):** the v2-data-preservation test (`skillet-rm-08`: open a v2 DB with rows, close, reopen, assert intact — no migration runs because the version is unchanged) and the v1 → v2 test (`skillet-rm-09`: `MigrationTestHelper` materializes a v1 DB from `1.json` with a seeded row, open the DB so the registered `MIGRATION_1_2` runs, assert the resulting schema matches `2.json` and the seeded v1 data is gone) are deferred, not closed, as the revival vehicle. The data-preservation requirement stays true by construction in the meantime (see Decision 8).
5. **Docs:** fix the two stale `AGENTS.md` schema claims.
6. **Verification gate (shrunk — no instrumented tests in this change):** `./gradlew :app:assembleDebug`, `./gradlew test`, and `./gradlew :app:lint` all green. `./gradlew test` still runs the existing JVM suite and must stay green, but **no new DB tests of any kind are planned** (the JVM vehicle is out of scope per Decision 1, and the instrumented vehicle is deferred to Room 3). Then confirm no schema version bump and `app/schemas/` unchanged.

Rollback: revert the code. No user data is affected (no schema change; the dev device's v2 DB is untouched by an unchanged schema).

## Open Questions

- **None blocking for this change's (deferred) scope.** For the Room 3 revival: the exact `MigrationTestHelper` API mechanics (constructor signature, the schema-directory argument, `migrate(...)` semantics) must be re-derived against the Room 3 API — the partial 2.8.4 learnings are recorded in the Deferred work section.
