# Design: Room 2.8.4 → 3.0.0 Migration (skillet-m5r)

## Context

Skillet persists recipes in a Room database (`RecipeDatabase`, version 2) with 6 entities,
4 DAOs, one explicit destructive migration (`MIGRATION_1_2`), an FK-pragma open callback,
and a Koin `createdAtStart()` single for the DB (any Room misconfiguration = launch crash,
blank app). The app currently compiles against Room 2.8.4 with KSP 2.3.10 (KAPT already
migrated; `ksp(libs.room.compiler)` in `app/build.gradle.kts:122`).

Blast radius (grep-verified, 15 files import `androidx.room`):

| File | Role in migration |
|---|---|
| `database/migrations/Migrations.kt` | `SupportSQLiteDatabase` migration |
| `database/RecipeDatabase.kt` | `@Database`, `@TypeConverters` |
| `database/MeasurementConverters.kt` | 2× `@TypeConverter` |
| `database/entity/*.kt` (6) | `@Entity` annotations (imports only) |
| `database/dao/*.kt` (4) | `@Dao` annotations (imports only) |
| `SkilletApp.kt` | `Room.databaseBuilder`, `fkPragmaCallback` |
| `data/RecipeRepositoryImpl.kt` | `withTransaction` usage |

Current build state (`gradle/libs.versions.toml`): `kotlin = "2.4.10"` (line 3),
`ksp = "2.3.10"` (line 4), `room = "2.8.4"` (line 14); Room libraries at lines 63–66
(`room-runtime`, `room-compiler`, `room-ktx`, `room-testing`); `androidx-room` Gradle
plugin ref at line 89 (`id = "androidx.room"`); bundle `room = ["room-runtime", "room-ktx"]`
(line 99). `app/build.gradle.kts`: KSP plugin (line 9), Room plugin (line 11),
`room { schemaDirectory("$projectDir/schemas") }` (line 62),
`implementation(libs.bundles.room)` + `ksp(libs.room.compiler)` (lines 121–122),
`androidTestImplementation(libs.room.testing)` (line 132).

The prior `room-migrations` change (archived 2026-09-23) left two instrumented tests
deferred to this migration: `skillet-rm-08` (v2 data preservation) and `skillet-rm-09`
(v1→v2 migration via `MigrationTestHelper`). Their Room 2.8.4 API mechanics
(`androidx.room.testing.MigrationTestHelper`) must be re-derived against the Room 3 API.

## Goals

- Run on Room 3.0.0 with **zero** `androidx.room` / `SupportSQLite` references in `app/src`.
- **Zero APK bloat**: platform SQLite driver, no bundled engine.
- **Data + schema invariance**: checked-in `app/schemas/.../2.json` stays byte-identical;
  DB version stays 2; no user data loss.
- Gate 1 and Gate 2 verifiable **without a device** (assembleDebug/test/lint/grep/diff).
- Restore the deferred instrumented migration tests (rm-08/rm-09) in Gate 3.

## Non-Goals

- No new features, no schema changes, no DB version bump.
- No changes to `:utils` or `:measurement` (pure JVM, zero Room).
- No JVM unit-test additions for Room (instrumented only, per existing test strategy).
- No CI or new infrastructure.

## Decisions

- **D1 (approved): Three gates.**
  - **Gate 1** (still on Room 2.8.4): convert all APIs to non-deprecated driver APIs.
  - **Gate 2** (Room 3.0.0): artifact swap, import sweep, suspend signatures.
  - **Gate 3** (instrumented, device required): spike the Room 3 migration-test API, then
    execute rm-08 → rm-09.
- **D2 (approved): `AndroidSQLiteDriver`** (platform engine; zero APK bloat; minSdk 30).
  Delivered by `androidx.sqlite:sqlite-android` — latest stable **2.7.1** (Google Maven
  metadata, checked 2026-09-09; 2.8.0-alpha01 is alpha, not used).
- **D3 (approved): KSP first.** Explicit KSP-audit task in Gate 1; conditional KSP catalog
  bump in Gate 2 (human pre-approved the bump if `room3-compiler` requires it).
- **D4: Set the driver last.** The guide is explicit: after `setDriver(...)`, Room disables
  compatibility mode and any remaining `SupportSQLiteDatabase` call throws at runtime.
  Gate 1 therefore converts migrations → callback → repository transactions *before* the
  builder adopts `setDriver`/`setQueryCoroutineContext`.
- **D5: Adopt `setQueryCoroutineContext(Dispatchers.IO)`** on the builder (guide-recommended
  replacement for a custom `Executor`; we set none today). Makes DAO dispatch explicit for
  the suspend DAO calls made from `viewModelScope` in `RecipeRepositoryImpl`.
- **D6: No DAO signature work.** Verified: every non-`Flow` DAO function is already
  `suspend` (all 4 DAOs, grep-verified); `Flow` return types are native in Room 3. No
  `@RawQuery`, no `InvalidationTracker.Observer`, no LiveData/Rx/Guava/Paging converters —
  all guide items that would otherwise require changes are not present in this codebase.
- **D7: `@Upsert` (7 sites) — guide is silent on Room 3 status.** Verify at compile in
  G2-VERIFY/G2-DEPS. Fallback if removed: flag for human review (no silent replacement;
  upsert semantics differ from `@Insert(onConflict = REPLACE)`).
- **D8: Reuse existing beads.** rm-08/rm-09 are un-deferred and wired into the new graph —
  no duplicate tracking of the same work.
- **D9: Schema contract.** `app/schemas/com.kronos.skilletapp.database.RecipeDatabase/2.json`
  must remain byte-identical (`git diff --stat app/schemas` → empty at each gate).
- **D10: Baseline-relative style gates (Option A).** `skillet-dlm` is OPEN and records
  pre-existing `:app` style failures (15 ktfmtCheck files, 9 detekt issues) that include two
  files this migration edits (`RecipeRepositoryImpl.kt`, `SkilletApp.kt`), while its fix scope
  (ui/*, parser/*, scraping/*, domain/*) is well outside the migration blast radius and is tied
  to the entity-domain-split branch merge. The per-gate gates are therefore baseline-relative:
  G1-AUDIT snapshots the pre-migration failure sets of `:app:ktfmtCheck`, `:app:detekt`,
  `:app:lint`, and `./gradlew test`; each gate then requires no NEW failures/findings vs. the
  snapshot, plus ktfmt/detekt green on every file the migration touched. Pre-existing baseline
  failures in untouched files remain owned by `skillet-dlm`, not this change (strict
  module-wide green would fail gates for pre-existing reasons; excluding style checks entirely
  would drop format control over the very files the migration edits). No scope expansion.

## Verified API Reference

Source: official "Migrate to Room 3.0" guide (local copy
`/tmp/opencode/room-migration-guide.html`), section-verified line by line.

### Phase 1 (Room 2.8.x) — exact forms

Migrations (`androidx.sqlite` package, not `androidx.sqlite.db`):

```kotlin
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL("...")
  }
}
```

Callbacks (guide example shows `onCreate`; `onOpen` follows the same overload pattern —
see Risk R4):

```kotlin
private val fkPragmaCallback = object : RoomDatabase.Callback() {
  override fun onOpen(connection: SQLiteConnection) {
    super.onOpen(connection)
    connection.execSQL("PRAGMA foreign_keys = ON")
  }
}
```

Transactions:

```kotlin
import androidx.room.useWriterConnection
import androidx.room.immediateTransaction

database.useWriterConnection { connection ->
  connection.immediateTransaction {
    // ... existing DAO calls (body must be a suspend lambda — DAO fns are suspend)
  }
}
```

Builder hardening (driver LAST):

```kotlin
Room.databaseBuilder(context, RecipeDatabase::class.java, "recipes.db")
  .addMigrations(MIGRATION_1_2)
  .addCallback(fkPragmaCallback)
  .setQueryCoroutineContext(Dispatchers.IO)
  .setDriver(/* AndroidSQLiteDriver() */)  // final step — disables compat mode
  .build()
```

### Phase 2 (Room 3.0) — exact forms

Catalog (guide lines 8792–8806):

```toml
[versions]
room3 = "3.0.0"

[libraries]
androidx-room3-runtime = { module = "androidx.room3:room3-runtime", version.ref = "room3" }
androidx-room3-compiler = { module = "androidx.room3:room3-compiler", version.ref = "room3" }
```

```kotlin
dependencies {
  implementation(libs.androidx.room3.runtime)
  ksp(libs.androidx.room3.compiler)
}
```

Imports: `import androidx.room.*` → `import androidx.room3.*`.

Type converters (renamed, package `androidx.room3`):

```kotlin
import androidx.room3.ColumnTypeConverter
import androidx.room3.ColumnTypeConverters

@ColumnTypeConverters(MeasurementConverters::class)  // was @TypeConverters
abstract class RecipeDatabase ...

@ColumnTypeConverter  // was @TypeConverter
```

Suspend callbacks + migrations (`execSQL` → `executeSQL`, moved to `androidx.sqlite.async`):

```kotlin
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL

override suspend fun migrate(connection: SQLiteConnection) { connection.executeSQL(...) }
override suspend fun onOpen(connection: SQLiteConnection) { ... }
```

Transactions: `useWriterConnection { immediateTransaction { ... } }` →
`withWriteTransaction { ... }` (guide note, Phase 1 transactions section).

## Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | `room3-compiler` incompatible with KSP 2.3.10 (KSP2 era) | G1-AUDIT baseline; conditional catalog bump in G2-DEPS (human pre-approved, D3) |
| R2 | `@Upsert` status in Room 3 unknown (guide silent) | Compile check in G2-DEPS; fallback = flag for human review (D7) |
| R3 | `setDriver` disables compat mode → any leftover `SupportSQLite` = launch crash (Koin `createdAtStart`) | Gate-1 ordering (D4): convert all three call sites, driver last; grep gate → zero |
| R4 | Guide's callback example shows `onCreate`; our app overrides `onOpen` — `onOpen(SQLiteConnection)` overload presumed present in 2.8.4 (scout audit confirms it as target) | Compile check in G1-CALLBACK; fallback: keep `SupportSQLiteDatabase` callback through Gate 1, move it to Gate 2 (G1 grep gate scoped accordingly) |
| R5 | `AndroidSQLiteDriver` FQN/artifact: guide only shows `androidx.sqlite.driver.bundled.BundledSQLiteDriver` import; Android driver lives in `androidx.sqlite:sqlite-android` (expected FQN `androidx.sqlite.driver.android.AndroidSQLiteDriver`) | G1-DRV: add `sqlite-android:2.7.1`, confirm FQN + alignment with Room's transitive `androidx.sqlite` core version via dependency graph; compile is the check |
| R6 | Room 3 Gradle plugin id, `room3-ktx` existence, `room3-testing` artifact name all unverified (V1/V2/V5) | G2-VERIFY resolves all from Google Maven metadata **before** any build-file edit |
| R7 | Room 3 KSP may regenerate `2.json` with different byte content (same schema) | `git diff --stat app/schemas` → empty at every gate; investigate any diff before proceeding |
| R8 | `MIGRATION_1_2` is destructive by design (v1 rows dropped) | Semantics unchanged by this migration; rm-09 test pins the contract (seeded v1 row gone, schema = 2.json) |
| R9 | Gate 3 requires a device; Gates 1–2 must stay device-free | Graph edges make Gate 3 beads depend on the Gate-2 gate bead; no Gate-1/2 task requires a device |
| R10 | Formatting/lint drift across a 15-file sweep; pre-existing ktfmt/detekt baseline failures (`skillet-dlm`, OPEN) include two files this migration edits | Baseline-relative gates per D10: G1-AUDIT snapshots pre-migration `:app:ktfmtCheck` / `:app:detekt` / `:app:lint` / `./gradlew test` failure sets; per-gate gates = assembleDebug green; test green with no new failures vs. snapshot; `:app:lint` green with no new findings vs. snapshot; ktfmtCheck/detekt green on every file the migration touched (newly touched files ktfmt-clean; pre-existing baseline failures in untouched files may remain); rg checks → 0; `git diff --stat app/schemas` → empty. ktfmt/detekt health of the rest of the module stays owned by `skillet-dlm`, not this change |
| R11 | Guide examples omit `suspend` on `immediateTransaction`/`useWriterConnection` lambdas; our body calls suspend DAO fns | G1-TX compile check; if 2.8's `immediateTransaction` is non-suspend, find the suspend variant (compile will name it) |

## Migration Plan

### Gate 1 — modernize on Room 2.8.4 (no device)

| # | Task | Target(s) | Verify |
|---|---|---|---|
| 1.1 | G1-AUDIT KSP baseline + pre-migration baseline snapshot: confirm KSP 2.3.10 ↔ Kotlin 2.4.10 pairing works for the current Room KSP path; record baseline in bead comment; ALSO snapshot the pre-migration baseline failure sets (file:rule lists) of `./gradlew :app:ktfmtCheck`, `./gradlew :app:detekt`, `./gradlew :app:lint`, and `./gradlew test` in a bead comment — the 1.6/2.5 gates are baseline-relative and compare against this snapshot (D10) | none (investigation) | `./gradlew :app:assembleDebug` green; KSP baseline + the four baseline failure sets recorded |
| 1.2 | G1-MIGRATIONS: `MIGRATION_1_2` → `SQLiteConnection` + `androidx.sqlite.execSQL` | `Migrations.kt` | compiles; DDL strings unchanged (byte-identical SQL) |
| 1.3 | G1-CALLBACK: `fkPragmaCallback.onOpen` → `SQLiteConnection` | `SkilletApp.kt:53-58` | compiles (R4 fallback noted) |
| 1.4 | G1-TX: `database.withTransaction` → `useWriterConnection { immediateTransaction { … } }` | `RecipeRepositoryImpl.kt:3,46-63` | compiles; transaction body unchanged |
| 1.5 | G1-DRV: add `androidx.sqlite:sqlite-android:2.7.1` (aligned to Room's transitive sqlite core); builder += `setQueryCoroutineContext(Dispatchers.IO)` then `setDriver(AndroidSQLiteDriver())` LAST | `libs.versions.toml`, `app/build.gradle.kts`, `SkilletApp.kt:60-68` | compiles; R5 checked |
| 1.6 | G1-GATE: full verification (baseline-relative; snapshot in the `skillet-g1-audit` comment) | — | 1) `./gradlew :app:assembleDebug` green; 2) `./gradlew test` green with no new failures vs. the G1-AUDIT baseline snapshot; 3) `./gradlew :app:lint` green with no new findings vs. the snapshot; 4) `./gradlew :app:ktfmtCheck` and `./gradlew :app:detekt` green on every file the migration touched (newly touched files must be ktfmt-clean; pre-existing baseline failures in untouched files may remain); 5) `rg "SupportSQLite" app/src` → 0 matches; 6) `git diff --stat app/schemas` → empty (schema 2.json byte-identical) |

Dependencies: 1.1 → {1.2, 1.3, 1.4} → 1.5 → 1.6. (1.2/1.3/1.4 may run in parallel.)

### Gate 2 — swap to Room 3.0.0 (no device)

| # | Task | Target(s) | Verify |
|---|---|---|---|
| 2.1 | G2-VERIFY: resolve V1 (Room 3 Gradle plugin id), V2 (`room3-ktx` exists?), V5-testing (`room3-testing` artifact name), `@Upsert` support, KSP requirement of `room3-compiler`; record all in bead comment before any edit | none | findings recorded; no build edits yet |
| 2.2 | G2-DEPS: catalog swap (`room3` version + `androidx.room3:*` libraries), plugin swap, `ksp(libs.androidx.room3.compiler)`, bundle swap, `androidTestImplementation` test-artifact swap; **conditional KSP bump** per 1.1/2.1 | `gradle/libs.versions.toml`, `app/build.gradle.kts` | dependency resolution succeeds; R1/R2 checked |
| 2.3 | G2-IMPORTS: sweep 15 files `androidx.room` → `androidx.room3`; fix 5 stale KDocs (4 DAOs "not yet registered in @Database or Koin", `MeasurementConverters.kt` "orphan converter class") | 15 source files | `rg "^import androidx\.room\." app/src` → 0; compiles |
| 2.4 | G2-SIG: `MIGRATION_1_2.migrate` → `suspend` + `androidx.sqlite.async.executeSQL`; `fkPragmaCallback.onOpen` → `suspend`; `useWriterConnection/immediateTransaction` → `withWriteTransaction`; `@TypeConverter(s)` → `@ColumnTypeConverter(s)` | `Migrations.kt`, `SkilletApp.kt`, `RecipeRepositoryImpl.kt`, `MeasurementConverters.kt`, `RecipeDatabase.kt` | compiles |
| 2.5 | G2-GATE: full verification (baseline-relative; snapshot in the `skillet-g1-audit` comment) | — | 1) `./gradlew :app:assembleDebug` green; 2) `./gradlew test` green with no new failures vs. the G1-AUDIT baseline snapshot; 3) `./gradlew :app:lint` green with no new findings vs. the snapshot; 4) `./gradlew :app:ktfmtCheck` and `./gradlew :app:detekt` green on every file the migration touched (newly touched files must be ktfmt-clean; pre-existing baseline failures in untouched files may remain); 5) `rg "SupportSQLite" app/src` → 0 matches; 6) `rg "^import androidx\.room\." app/src` → 0 matches (no non-room3 Room imports); 7) `git diff --stat app/schemas` → empty (schema 2.json byte-identical) |
| 2.6 | G2-DOCS: update `AGENTS.md` (Room 3, plugin, driver, migration-test notes) | `AGENTS.md` | docs match build files |

Dependencies: 1.6 → 2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.6 (strict chain).

### Gate 3 — instrumented migration tests (device required)

| # | Task | Target(s) | Verify |
|---|---|---|---|
| 3.1 | G3-SPIKE: re-derive Room 3 migration-test mechanics — test artifact (from 2.1), helper class FQN, schema-directory argument, `migrate(1)` usage, suspend-ness; minimal smoke androidTest; record mechanics in bead comment (V4: how schema JSON reaches the test — assets copy vs direct path) | `app/src/androidTest/...` (scratch) | green on device via `./gradlew :app:connectedDebugAndroidTest`; if unreachable, STOP + flag human |
| 3.2 | G3-RM08: un-defer `skillet-rm-08`; add graph edges (spike blocks rm-08); execute: v2 data-preservation test (open v2 DB, seed rows, close, reopen, assert intact) | `app/src/androidTest/...` | `connectedDebugAndroidTest` green |
| 3.3 | G3-RM09: un-defer `skillet-rm-09`; add graph edges (rm-08 blocks rm-09); execute: `migrate(1)` from `1.json`, seed v1 row, open DB so `MIGRATION_1_2` runs, assert schema = `2.json` + seeded row gone (destructive contract) | `app/src/androidTest/...` | `connectedDebugAndroidTest` green |
| 3.4 | G3-GATE: full instrumented suite | — | `./gradlew :app:connectedDebugAndroidTest` fully green (includes existing `ExampleInstrumentedTest`) |

Dependencies: 2.6 → 3.1 → 3.2 → 3.3 → 3.4 (strict chain; 3.2/3.3 reuse existing beads).

## Open Questions (resolved in-graph, not blocking planning)

| # | Question | Resolved by |
|---|---|---|
| V1 | Room 3 Gradle plugin id (`androidx.room3`? same `androidx.room` at 3.0.0?) | 2.1 (G2-VERIFY) |
| V2 | Does `androidx.room3:room3-ktx` exist? (bundle currently includes `room-ktx`) | 2.1 (G2-VERIFY) |
| V3 | ~~SQLite driver artifact + version~~ → **RESOLVED**: `androidx.sqlite:sqlite-android` 2.7.1 stable | — |
| V4 | How schema JSON reaches instrumented tests (assets copy task vs direct file path) | 3.1 (G3-SPIKE) |
| V5 | Room 3 `MigrationTestHelper` API (artifact/class/schema-dir/suspend) | 2.1 (artifact name) + 3.1 (mechanics) |

## Acceptance Criteria (end state)

1. `app/src` contains zero `androidx.room` (non-`room3`) imports and zero `SupportSQLite` references.
2. `gradle/libs.versions.toml` has no `room = "2.8.4"` entry used by `:app` (replaced by `room3`).
3. `./gradlew :app:assembleDebug` green; `./gradlew test` green with no new failures vs. the
   G1-AUDIT baseline snapshot; `./gradlew :app:lint` green with no new findings vs. the snapshot.
4. `git diff --stat app/schemas` is empty vs. the pre-migration commit.
5. `./gradlew :app:connectedDebugAndroidTest` green including rm-08 and rm-09 tests.
6. `skillet-rm-08`, `skillet-rm-09`, and all new beads closed; `skillet-m5r` closed on completion.
7. `AGENTS.md` accurately describes the Room 3 setup.
