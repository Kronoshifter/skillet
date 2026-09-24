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
- **Data + schema contract**: checked-in `app/schemas/.../2.json` stays byte-identical at
  every gate; no user data loss. The only schema change in this change is the v3 index cut
  (task 3.4, D12): the 8 captured indexes land as a single 2→3 version bump, so the final
  schema is `3.json` = `2.json` + exactly those 8 indexes.
- Gate 1 and Gate 2 verifiable **without a device** (assembleDebug/test/lint/grep/diff).
- Restore the deferred instrumented migration tests (rm-08/rm-09) in Gate 3.

## Non-Goals

- No new features. No schema changes except the single v3 index cut (task 3.4, D12 — the
  rescoped parallel-workstream index spec, now owned by this change).
- No changes to `:utils` or `:measurement` (pure JVM, zero Room).
- No JVM unit-test additions for Room (instrumented only, per existing test strategy).
- No CI or new infrastructure.

## Decisions

- **D1 (approved): Three gates.**
  - **Gate 1** (still on Room 2.8.4): convert all APIs to non-deprecated driver APIs.
  - **Gate 2** (Room 3.0.0): artifact swap, import sweep, suspend signatures.
  - **Gate 3** (instrumented, device required): spike the Room 3 migration-test API, then
    execute rm-08 → rm-09 → the v3 schema cut (3.4, D12).
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
  must remain byte-identical at every gate up to and including the v3 cut
  (`git diff --stat app/schemas` → no `2.json` diff at any gate; the v3 cut (3.4, D12) adds
  `3.json` as a NEW file without modifying `2.json`).
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
  Baseline measured at G1-AUDIT (2026-09-24): ktfmt 29 files (26 main + 3 test), detekt
  config-validation abort / zero findings, lint green (0 errors/29 warnings/1 hint), test
  37/2 (network). Earlier '~15 files / ~9 detekt issues' estimates were stale.
- **D11: Gate clauses rescoped to measured G1-AUDIT baseline (2026-09-24).** The measured
  G1-AUDIT baseline (authoritative, recorded on `skillet-g1-audit`) is red at canonical gate
  item 2 (`./gradlew test`: 37 tests, 2 failures — both in `RecipeScrapingTests`,
  network-dependent live-URL scrapers) and at item 4's `:app:detekt` half (aborts at config
  validation before any file is analyzed — `style>MaxLineLength>excludeExtensionFunctionHints`
  is SET IN THE REPO `detekt.yml` but not recognized by `dev.detekt` 2.0.0-alpha.6, so the
  run produces zero findings and fails on the invalid property; zero findings). Both are
  pre-existing and migration-unrelated, so as written the two clauses could never hold
  regardless of migration quality: item 2 is rescoped to "no NEW failures vs. the G1-AUDIT
  baseline snapshot" (every failing test at gate time must be a member of the baseline
  failing set), and item 4's detekt half is rescoped to "behavior UNCHANGED from the
  G1-AUDIT snapshot" (config-validation abort, zero findings; this migration must not alter
  detekt config or catalog; the fix — remove/rename the invalid property in `detekt.yml`,
  or pin/upgrade detekt to a version that supports it — is owned by `skillet-dlm`, now
  carried there as a first-step item). Rest of D10 unchanged.
  NOTE (2026-09-24 post-close correction): a one-line removal of that property is ALREADY
  PENDING in the working tree (uncommitted), alongside unrelated entity-index changes and a
  regenerated `app/schemas/.../2.json` — a parallel workstream, not this migration. If
  still present at gate time, items 4 (detekt behavior) and 6 (schema byte-identical) would
  fail for pre-existing, migration-unrelated reasons (same class as this rescoping). For
  Oracle/owner decision: commit or shelve the parallel workstream before gates; if the
  detekt.yml fix lands, the detekt half of item 4 needs a re-baseline (capture the real
   `:app:detekt` findings set). Flagged on `skillet-g1-audit`; not acted on here.
- **D12: The rescoped v3 index cut is an explicit 2→3 version bump (2026-09-24 rescope).**
  A parallel workstream captured a unique-index set on all 6 entities (8 indexes total; full
  spec captured verbatim on `skillet-g3-schema`). That session's working-tree changes were
  reverted before capture completed, so the bead carries the authoritative spec. Mechanism:
  explicit `MIGRATION_2_3` in `database/migrations/Migrations.kt` in the Room 3 suspend form
  (`override suspend fun migrate(db: SQLiteConnection)` + `androidx.sqlite.async.executeSQL` —
  the same form G2-SIG leaves `MIGRATION_1_2` in; this task runs after Gate 2), registered
  via `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)` in `SkilletApp.kt`;
  `@Database(version = 3)`; `autoMigrations` stays `[]` (consistent with the existing
  `MIGRATION_1_2` explicit-migration pattern). Auto-migration rejected: a new style for this
  codebase, an opaque generated class, and the 8 DDL statements are fully determined by the
  captured spec. `2.json` stays byte-identical throughout; KSP emits `3.json` and it must be
  KSP-stable across consecutive builds.
  **FINDING F-1 (CRITICAL — RESOLVED 2026-09-24 by human decision: option (a), 8 NON-UNIQUE
  indexes; finding text preserved below as permanent rationale):** the captured
  spec is `unique = true` on the foreign-key columns of MULTI-ROW tables. The write path
  (`data/RecipeMapper.kt` lines 34–95) proves ingredient/instruction/equipment and both join
  tables hold multiple rows per recipe/instruction — 7 of the 8 unique columns (all except
  `recipe.name`) are violated by ANY realistic recipe (2+ instructions or 2+ ingredients), so
  implemented as captured the 2→3 migration would fail on first open of every device with
  real data (migration abort → Koin `createdAtStart()` failure → blank app; the app has no
  user-visible migration-error path today). The directive's default (hard-fail on duplicate
  data) is therefore UNACCEPTABLE AS A DEFAULT for this spec. Original options for the record:
  (a) 8 NON-UNIQUE indexes — keeps the FK-lookup indexing intent, zero data risk;
  (b) keep `unique` as captured — not viable per the above; (c) a different uniqueness column
  set (e.g. per-recipe ordering) — a separate domain decision.
  **F-1 RESOLUTION (2026-09-24, human):** option (a) — all 8 indexes land as plain NON-UNIQUE
  `Index(value = [...])` (no `unique = true`). Consequences: no duplicate/violation-data
  failure mode exists — `MIGRATION_2_3` cannot fail on data; its DDL is still 8 ×
  `CREATE INDEX IF NOT EXISTS` (non-unique form), required so the post-migration schema
  matches `3.json`; the new instrumented test asserts all 8 indexes present and non-unique,
  schema = `3.json`, v2 rows intact, and has NO violation-data case (none is possible).
  Note (one line, not a requirement): `recipe.name` is NON-UNIQUE as well — name-uniqueness
  is intentionally NOT enforced by the schema and is deferred as a separate domain decision
  (option (c) territory).

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
| R7 | Room 3 KSP may regenerate `2.json` with different byte content (same schema) | `git diff --stat app/schemas` shows no `2.json` diff at any gate; from the v3 cut (3.4, D12) the end state is `2.json` byte-identical + NEW `3.json` (KSP-stable); investigate any `2.json` diff before proceeding |
| R8 | `MIGRATION_1_2` is destructive by design (v1 rows dropped) | Semantics unchanged by this migration; rm-09 test pins the contract (seeded v1 row gone after the full chain; final schema = `3.json`, which is `2.json` + the 8 captured indexes) |
| R9 | Gate 3 requires a device; Gates 1–2 must stay device-free | Graph edges make Gate 3 beads depend on the Gate-2 gate bead; no Gate-1/2 task requires a device |
| R10 | Formatting/lint drift across a 15-file sweep; pre-existing ktfmt/detekt baseline failures (`skillet-dlm`, OPEN) include two files this migration edits | Baseline-relative gates per D10, with item 2 and item 4's detekt half rescoped per D11 against the measured G1-AUDIT baseline (recorded on `skillet-g1-audit`): per-gate gates = assembleDebug green; `./gradlew test` — no NEW failures vs. the snapshot (baseline: 37 tests, 2 network-dependent `RecipeScrapingTests` failures; every failing test at gate time a member of the baseline failing set); `:app:lint` green with no new findings vs. the snapshot; `:app:ktfmtCheck` green on every file the migration touched (newly touched files ktfmt-clean; the 29-file pre-existing baseline elsewhere may remain); `:app:detekt` behavior UNCHANGED from the snapshot (config-validation abort, zero findings — detekt config/catalog must not be altered by this migration; the tooling fix is owned by `skillet-dlm`); rg checks → 0; `git diff --stat app/schemas` → empty. ktfmt/detekt health of the rest of the module stays owned by `skillet-dlm`, not this change |
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
| 1.6 | G1-GATE: full verification (baseline-relative; snapshot in the `skillet-g1-audit` comment) | — | 1) `./gradlew :app:assembleDebug` green; 2) `./gradlew test` — no NEW failures vs. the G1-AUDIT baseline snapshot (baseline: 37 tests, 2 failures, both in `RecipeScrapingTests`, network-dependent; every failing test at gate time must be a member of the baseline failing set); 3) `./gradlew :app:lint` green with no new findings vs. the snapshot; 4) `./gradlew :app:ktfmtCheck` green on every file the migration touched (newly touched files must be ktfmt-clean; the 29-file pre-existing baseline elsewhere may remain); `./gradlew :app:detekt` behavior UNCHANGED from the G1-AUDIT snapshot (config-validation abort, zero findings — this migration must not alter detekt config or catalog; the tooling fix is owned by `skillet-dlm`); 5) `rg "SupportSQLite" app/src` → 0 matches; 6) `git diff --stat app/schemas` → empty (schema 2.json byte-identical) |

Dependencies: 1.1 → {1.2, 1.3, 1.4} → 1.5 → 1.6. (1.2/1.3/1.4 may run in parallel.)

### Gate 2 — swap to Room 3.0.0 (no device)

| # | Task | Target(s) | Verify |
|---|---|---|---|
| 2.1 | G2-VERIFY: resolve V1 (Room 3 Gradle plugin id), V2 (`room3-ktx` exists?), V5-testing (`room3-testing` artifact name), `@Upsert` support, KSP requirement of `room3-compiler`; record all in bead comment before any edit | none | findings recorded; no build edits yet |
| 2.2 | G2-DEPS: catalog swap (`room3` version + `androidx.room3:*` libraries), plugin swap, `ksp(libs.androidx.room3.compiler)`, bundle swap, `androidTestImplementation` test-artifact swap; **conditional KSP bump** per 1.1/2.1 | `gradle/libs.versions.toml`, `app/build.gradle.kts` | dependency resolution succeeds; R1/R2 checked |
| 2.3 | G2-IMPORTS: sweep 15 files `androidx.room` → `androidx.room3`; fix 5 stale KDocs (4 DAOs "not yet registered in @Database or Koin", `MeasurementConverters.kt` "orphan converter class") | 15 source files | `rg "^import androidx\.room\." app/src` → 0; compiles |
| 2.4 | G2-SIG: `MIGRATION_1_2.migrate` → `suspend` + `androidx.sqlite.async.executeSQL`; `fkPragmaCallback.onOpen` → `suspend`; `useWriterConnection/immediateTransaction` → `withWriteTransaction`; `@TypeConverter(s)` → `@ColumnTypeConverter(s)` | `Migrations.kt`, `SkilletApp.kt`, `RecipeRepositoryImpl.kt`, `MeasurementConverters.kt`, `RecipeDatabase.kt` | compiles |
| 2.5 | G2-GATE: full verification (baseline-relative; snapshot in the `skillet-g1-audit` comment) | — | 1) `./gradlew :app:assembleDebug` green; 2) `./gradlew test` — no NEW failures vs. the G1-AUDIT baseline snapshot (baseline: 37 tests, 2 failures, both in `RecipeScrapingTests`, network-dependent; every failing test at gate time must be a member of the baseline failing set); 3) `./gradlew :app:lint` green with no new findings vs. the snapshot; 4) `./gradlew :app:ktfmtCheck` green on every file the migration touched (newly touched files must be ktfmt-clean; the 29-file pre-existing baseline elsewhere may remain); `./gradlew :app:detekt` behavior UNCHANGED from the G1-AUDIT snapshot (config-validation abort, zero findings — this migration must not alter detekt config or catalog; the tooling fix is owned by `skillet-dlm`); 5) `rg "SupportSQLite" app/src` → 0 matches; 6) `rg "^import androidx\.room\." app/src` → 0 matches (no non-room3 Room imports); 7) `git diff --stat app/schemas` → empty (schema 2.json byte-identical) |
| 2.6 | G2-DOCS: update `AGENTS.md` (Room 3, plugin, driver, migration-test notes) | `AGENTS.md` | docs match build files |

Dependencies: 1.6 → 2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.6 (strict chain).

### Gate 3 — instrumented migration tests (device required)

| # | Task | Target(s) | Verify |
|---|---|---|---|
| 3.1 | G3-SPIKE: re-derive Room 3 migration-test mechanics — test artifact (from 2.1), helper class FQN, schema-directory argument, `migrate(1)` usage, suspend-ness; minimal smoke androidTest; record mechanics in bead comment (V4: how schema JSON reaches the test — assets copy vs direct path) | `app/src/androidTest/...` (scratch) | green on device via `./gradlew :app:connectedDebugAndroidTest`; if unreachable, STOP + flag human |
| 3.2 | G3-RM08: un-defer `skillet-rm-08`; add graph edges (spike blocks rm-08); execute: v2 data-preservation test (open v2 DB, seed rows, close, reopen, assert intact) | `app/src/androidTest/...` | `connectedDebugAndroidTest` green |
| 3.3 | G3-RM09: un-defer `skillet-rm-09`; add graph edges (rm-08 blocks rm-09); execute: `migrate(1)` from `1.json`, seed v1 row, open DB so the registered chain runs (`MIGRATION_1_2` → `MIGRATION_2_3`; the v3 cut from 3.4 is in place before this task), assert final schema = `3.json` + seeded v1 row gone (destructive contract at the 1→2 step; rm-08's v2 data-preservation assertion stays valid — rows intact across 2→3 too) | `app/src/androidTest/...` | `connectedDebugAndroidTest` green |
| 3.4 | G3-SCHEMA: the rescoped v3 schema cut (D12; spec verbatim on `skillet-g3-schema`): apply the 8 captured `@Index` annotations as NON-UNIQUE (per F-1 resolution, 2026-09-24) to the 6 entities + `@Database(version = 3)`; KSP: `2.json` byte-identical, `3.json` emitted with exactly the 8 captured indexes, KSP-stable; explicit `MIGRATION_2_3` (Room 3 suspend form) creating the 8 NON-UNIQUE indexes (8 × `CREATE INDEX IF NOT EXISTS`), registered via `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)`; duplicate/violation-data behavior: N/A (non-unique per F-1 resolution, no data-dependent failure mode); new instrumented test (v2 seed → chain applies 2→3 → schema = `3.json`, rows intact, all 8 indexes present and NON-UNIQUE; no violation-data case — none possible) | `database/entity/*.kt` (6), `database/RecipeDatabase.kt`, `database/migrations/Migrations.kt`, `SkilletApp.kt`, `app/src/androidTest/...`, `AGENTS.md` (Room/schema notes: v3, `2.json`/`3.json`, both migrations) | `:app:assembleDebug` green; `git diff --stat` on `2.json` empty; `3.json` present + KSP-stable; `connectedDebugAndroidTest` green incl. the new v3 test; `AGENTS.md` updated |
| 3.5 | G3-GATE: full instrumented suite | — | `./gradlew :app:connectedDebugAndroidTest` fully green (includes existing `ExampleInstrumentedTest`, rm-08, rm-09, and the v3 schema-cut test) |

Dependencies: 2.6 → 3.1 → 3.2 → 3.3 → 3.4 → 3.5 (strict chain; 3.2/3.3 reuse existing beads;
3.4 is `skillet-g3-schema`, created 2026-09-24; the F-1 uniqueness decision was resolved
2026-09-24 — 8 NON-UNIQUE indexes, option (a) — and the bead is no longer flagged `human`).

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
3. `./gradlew :app:assembleDebug` green; `./gradlew test` shows no NEW failures vs. the
   G1-AUDIT baseline snapshot (baseline: 37 tests, 2 failures, both in `RecipeScrapingTests`,
   network-dependent; every failing test at close time must be a member of the baseline
   failing set); `./gradlew :app:lint` green with no new findings vs. the snapshot.
4. `git diff --stat app/schemas` shows `2.json` unchanged vs. the pre-migration commit, plus
   NEW `3.json` (the v3 index cut, D12).
5. `./gradlew :app:connectedDebugAndroidTest` green including rm-08, rm-09, and the
   v3 schema-cut test (3.4).
6. `skillet-rm-08`, `skillet-rm-09`, `skillet-g3-schema`, and all new beads closed;
   `skillet-m5r` closed on completion.
7. `AGENTS.md` accurately describes the Room 3 setup.
