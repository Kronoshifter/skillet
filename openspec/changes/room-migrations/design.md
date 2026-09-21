## Context

The Room database (`RecipeDatabase`) is `version = 2` with a 6-table relational schema and **no** `autoMigrations` and **no** registered `Migration` objects. `SkilletApp.kt` opens it via `Room.databaseBuilder(...).fallbackToDestructiveMigration(true).addCallback(fkPragmaCallback).build()` and binds it as a Koin `createdAtStart()` single, so it opens at app launch. `room { schemaDirectory("$projectDir/schemas") }` is configured and both `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/1.json` and `2.json` are checked in.

The v1 → v2 jump (commit `1dc5e76`, part of `split-domain-entity-models`) was deliberately destructive: v1 was a single `recipe` table with `ingredients`/`instructions`/`equipment` as JSON blobs in TEXT columns; the v1 serializers (`RecipeConverters`, commit `6990790`) are deleted, so a data-preserving v1 → v2 is not recoverable in any case. No v1 database with real data survives.

Constraints: Room **2.8.4** (auto-migrations and the instrumented `androidx.room:room-testing` / `MigrationTestHelper` path are both supported; no Room upgrade). **This change ships no schema bump** — the schema stays v2. A device/emulator IS available, so the final gate may run `connectedAndroidTest`.

## Goals / Non-Goals

**Goals:**
- Make the migration chain **complete and unbroken** from version 1 to the current version 2, with the v1 → v2 step explicit.
- Remove the destructive fallback so any future gap **fails loudly** (launch crash) instead of silently wiping `recipes.db`.
- Declare `autoMigrations` so future resolvable bumps are auto-generated and future unresolvable bumps fail loudly.
- Add the project's **first migration tests** as instrumented (`androidTest`) tests, with a spike-first strategy for the one hard unknown (Room opening on-device in this environment).
- Correct the two stale `AGENTS.md` schema claims.

**Non-Goals:**
- **No schema change** — version stays 2; no new/removed/renamed columns or tables; `app/schemas/` and `room.schemaLocation` are untouched.
- No Room version bump (2.8.4 has everything).
- No changes to entities, DAOs, repository, domain, ViewModels, or UI.
- No data-preserving v1 → v2 migration (impossible — v1 format deleted; accepted).
- **No JVM unit-test database vehicle** — a JVM-loadable SQLite driver is unreachable from `:app` local unit tests in this environment (see Decision 1, pivot evidence). Extracting the Room database into a pure-Kotlin module to enable a JVM path is a **future optimization only, explicitly out of scope** — it intersects the in-flight `split-domain-entity-models` change.
- No CI / pre-commit hooks (out of scope; correctness rests on locally-run tests, including a connected-device run).

## Decisions

**1. Test vehicle: instrumented `MigrationTestHelper` (on-device) first — spike first (durable; supersedes the original JVM-first decision).**

The original plan made a pure-JVM vehicle primary (`Room.testing()` + the KMP `androidx.sqlite:sqlite` driver's `jvm` variant on `:app`'s unit-test classpath, with `MigrationTestHelper` as the documented fallback). That premise was **definitively disproven** by a 3-round verification (evidence recorded on `skillet-rm-01`):

1. **Round 1** — plain `testImplementation(libs.androidx.sqlite)` (2.7.1) resolves on `debugUnitTestRuntimeClasspath` to the **android** variant (`sqlite-android`), not the JVM one: AGP 9.3.1's unit-test classpath requests `org.gradle.jvm.environment=android`.
2. **Round 2** — forcing the `jvm` variant via `JvmAttributes.JVM_ENVIRONMENT_ATTRIBUTE` fails at Kotlin-DSL script compile: `org.gradle.api.attributes.JvmAttributes` is not on Gradle 9's `.kts` script-compile classpath (implementation jar, not `-api`).
3. **Round 3** — spelling the attribute as `Attribute.of("org.gradle.jvm.environment", String::class.java), "jvm"` **compiles** but resolution fails `No matching variant`: the consumer requires BOTH `jvm.environment=jvm` (forced) AND `kotlin.platform.type=androidJvm` (AGP-pinned); the KMP artifact's `jvm` variant offers `platform.type=jvm` only, its android variant offers `env=android` only. No variant satisfies both.

**Conclusion: a JVM-loadable SQLite driver is unreachable from `:app` local unit tests in this environment.** The plan therefore pivots (human-authorized):

- **Primary vehicle: instrumented `androidx.room:room-testing` (`MigrationTestHelper`) tests** in `app/src/androidTest/`, driven by the checked-in schema JSONs in `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/` (v1 and `2.json`). `MigrationTestHelper` materializes real databases from those JSON files and runs the app's **registered** `Migration` objects against them — exactly the v1 → v2 contract this change introduces.
- **Rationale (durable):** a device/emulator is available and the final gate runs `./gradlew :app:connectedDebugAndroidTest`, so the instrumented vehicle is fully executable here. It exercises the real on-device path (real `Room.databaseBuilder`, real registered migrations, real checked-in schema files) — higher fidelity than a JVM driver shim would have been.
- **The spike is Task 1.2 and gates the test tasks:** an **instrumented smoke test** proves (a) the real `RecipeDatabase` opens on-device via `Room.databaseBuilder` and a row survives close/reopen, and (b) `MigrationTestHelper` can be constructed and can read the v1/v2 schema files from the checked-in schema directory. If the smoke test fails, the plan stops for human review — everything downstream assumes this vehicle works.
- **Exact `MigrationTestHelper` API mechanics** (constructor arguments, the schema-directory argument, how `migrate(1)` materializes a v1 DB) are confirmed by the smoke test and recorded in the test it produces — not assumed here.
- **Future optimization (out of scope):** a JVM vehicle becomes viable if the Room database is extracted into a pure-Kotlin module (the only way off the AGP-pinned unit-test classpath). That refactor intersects the in-flight `split-domain-entity-models` change and is explicitly deferred — noted here so the next planner does not re-litigate the pivot.
- Alternative (historical): JVM `Room.testing()` first. Rejected — see the 3-round evidence above; the premise is false in this environment, not merely risky.
- Alternative: a third-party JVM SQLite (e.g. `org.xerial:sqlite-jdbc`). Rejected for the same reason — any JVM driver on the `:app` unit-test classpath is blocked by the AGP platform-attribute pin, and it would also lose schema/behavior fidelity vs. Room's own driver.

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

**6. Test dependency: `androidx.room:room-testing:2.8.4` (androidTest-only).**
Add a `room-testing` library entry to `gradle/libs.versions.toml` (`androidx.room:room-testing`, `version.ref = room` — i.e. the existing `room` 2.8.4 version entry; no new version entry) and an `androidTestImplementation(libs.room.testing)` line in `app/build.gradle.kts`.
- **Rationale:** androidTest scope keeps the production APK clean; `room-testing` is the artifact Room itself ships for `MigrationTestHelper`, and pinning it to the same 2.8.4 as `room-runtime` guarantees schema/behavior fidelity.
- **The obsolete Round-1 edits are reverted as part of this task:** the uncommitted `androidx-sqlite = "2.7.1"` version entry + `androidx-sqlite` library entry in `gradle/libs.versions.toml` and `testImplementation(libs.androidx.sqlite)` in `app/build.gradle.kts` come out with the JVM driver. The earlier suggested commit message for those edits ("build(app): add androidx.sqlite 2.7.1 test driver dependency") is dead — do not reuse it.

**7. FK pragma vs. migration ordering — invariant, not coupling (edge case 3).**
Room runs migrations with `foreign_keys` OFF; the app's `PRAGMA foreign_keys = ON` is set in `fkPragmaCallback.onOpen`, which fires **after** migrations, per connection. Auto-generated and the v1 → v2 DDL are unaffected. **Invariant:** the pragma is per-connection and set in `onOpen`; migration code SHALL NOT assume `foreign_keys` is ON and SHALL NOT couple to the pragma callback. (A future custom migration that moves data across FK-related tables may toggle the pragma itself.) No action in this change beyond keeping the callback registered and noting the invariant.

## Risks / Trade-offs

- **[No device/emulator when the gate runs]** → a device/emulator is confirmed available (human-authorized); the final gate includes `./gradlew :app:connectedDebugAndroidTest`. If a run is impossible, the gate reports the blocker explicitly instead of silently passing — the instrumented tests are the gate, not an optional extra.
- **[Hand-written v2 DDL in `Migration(1,2)` drifts from the entities]** → the migration test asserts the post-migration schema matches the checked-in `2.json` (via `MigrationTestHelper`'s schema comparison), so drift fails the test rather than shipping.
- **[A missing future migration now crashes at launch (`createdAtStart`), i.e. a blank-screen app]** → that is the intended loud failure; the mitigation is the complete chain plus the v2-data-preservation test that opens the DB with existing data and asserts survival.
- **[Incidental schema drift during implementation silently becomes a migration]** → this change must not bump the version or alter the schema; any stray column/annotation change is a review blocker (edge case 4).
- **[`autoMigrations` "registered" is mistaken for "data migrations handled" for TEXT columns]** → captured as an explicit spec requirement (Decision 3 / delta spec) so the warning is inherited by future changes.
- **[Instrumented tests pollute or destroy the real on-device `recipes.db`]** → instrumented tests operate on test-harness databases (scratch files managed by `MigrationTestHelper`, or per-test databases cleaned up via `context.deleteDatabase(...)`); no checked-in `.db` files, and the app's `recipes.db` is never the test target.

## Migration Plan

1. **Instrumented deps + smoke test (de-risk):** revert the two obsolete JVM-driver files; add the `room-testing` catalog entry (`version.ref = room`, 2.8.4) + `androidTestImplementation`; verify the androidTest classpath resolves `androidx.room:room-testing:2.8.4`. Then the **instrumented smoke test**: Room opens on-device, a row survives close/reopen, and `MigrationTestHelper` reads the v1/v2 schema files from the checked-in schema directory. **If it fails, stop for human review.**
2. **Migration code:** add `database/migrations/` with `MIGRATION_1_2` (destructive: drop v1 `recipe`, recreate the six v2 tables; DDL from the entities, matching `2.json`).
3. **Wire it up:** add `autoMigrations` (empty) to `@Database` (version stays 2); in `SkilletApp.kt` add `.addMigrations(MIGRATION_1_2)` and **remove** `.fallbackToDestructiveMigration(true)`; keep `fkPragmaCallback`.
4. **Tests:** the v2-data-preservation test (plain instrumented Room test: open a v2 DB with rows, close, reopen, assert intact — no migration runs because the version is unchanged) + a v1 → v2 test (`MigrationTestHelper`: materialize a v1 DB from `1.json` with a seeded row, open the DB so the registered `MIGRATION_1_2` runs, assert the resulting schema matches `2.json` and the seeded v1 data is gone).
5. **Docs:** fix the two stale `AGENTS.md` schema claims.
6. **Verification gate:** `./gradlew :app:assembleDebug`, `./gradlew :app:connectedDebugAndroidTest`, `./gradlew test`, and `./gradlew :app:lint` all green. `./gradlew test` still runs the existing JVM suite and must stay green, but **no new JVM DB tests are planned** (the JVM vehicle is out of scope per Decision 1). Then confirm no schema version bump and `app/schemas/` unchanged.

Rollback: revert the code. No user data is affected (no schema change; the dev device's v2 DB is untouched by an unchanged schema).

## Open Questions

- **None blocking.** The exact `MigrationTestHelper` API mechanics (constructor signature, the schema-directory argument, `migrate(1)` semantics) are answered by the smoke test (Task 1.2) and the test tasks without changing the spec, the chosen approach, or the task breakdown.
