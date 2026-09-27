# Proposal: room3-migration

## Why

Room 3.0.0 — the KMP-first rewrite of Room — has shipped, and this app is pinned to Room 2.8.4
using 2.x-only APIs that the Room 3 API surface removes or re-names (`SupportSQLiteDatabase`-based
migrations and callbacks, `withTransaction`, `@TypeConverter`, non-suspend `migrate`/`onOpen`).
Staying on 2.8.4 forecloses the KMP path for the database and leaves the app on an API surface the
official migration guide treats as transitional. Additionally, the two deferred instrumented
migration tests (`skillet-rm-08`, `skillet-rm-09`) were blocked on a 2.8.4 testing API that Room 3
replaces; the migration is the moment to revive them against the new `MigrationTestHelper` API.

## What Changes

**Gate 1 — modernize on Room 2.8.4 (no version bump).** Every target API still exists in 2.8.4;
this gate makes the later cutover a mechanical rename.

- `MIGRATION_1_2` migrates on `SQLiteConnection` via `connection.execSQL` instead of
  `SupportSQLiteDatabase`/`db.execSQL`.
- `fkPragmaCallback` overrides `onOpen(connection: SQLiteConnection)`; the builder gains
  `.setQueryCoroutineContext(Dispatchers.IO)` and — as the **last** builder call —
  `.setDriver(AndroidSQLiteDriver())`, opting out of the deprecated `SupportSQLite` compat mode.
- New explicit dependency `androidx.sqlite:sqlite-android` (V3 resolved: latest stable **2.7.1**
  per Google Maven metadata, 2026-09-09; version independent of Room's) so the platform-provided
  driver is explicit — zero APK bloat, no bundled engine.
- `RecipeRepositoryImpl.upsert` transaction: `database.withTransaction { … }` →
  `database.useWriterConnection { conn -> conn.immediateTransaction { … } }`.
- KSP/Kotlin pairing audit: KSP `2.3.10` vs Kotlin `2.4.10` vs the upcoming Room 3 KSP2-era
  processor (pre-authorized catalog bump if incompatible).

**Gate 2 — the Room 3.0.0 cutover.**

- **BREAKING (internal only)**: `androidx.room:room-*` 2.8.4 artifacts (runtime/ktx/compiler/
  testing) → `androidx.room3:room3-*` 3.0.0 artifacts. KSP only — Room 3 has no kapt path.
  `room3-ktx` exists only if verified (V2); coroutines are first-class in the 3.0 bundle.
- `import androidx.room.*` → `import androidx.room3.*` across the **15 verified files**: 6
  entities, 4 DAOs, `RecipeDatabase`, `Migrations`, `MeasurementConverters`, `SkilletApp`,
  `RecipeRepositoryImpl` (grep-verified count; the scouting audit's "16" header was off by one).
- `@TypeConverter` → `@ColumnTypeConverter` (2 methods), `@TypeConverters` →
  `@ColumnTypeConverters` (1 site).
- `migrate(connection)` and the FK-pragma `onOpen(connection)` become `suspend`, and `execSQL`
  → `executeSQL`.
- `useWriterConnection { immediateTransaction { … } }` → `withWriteTransaction { … }`.
- Room Gradle plugin re-pointed at the Room 3 plugin with `room { schemaDirectory(...) }` kept
  (exact plugin id / task names resolved by the Gate 2 verification task, V1/V4).
- The exported v2 schema (`app/schemas/…/RecipeDatabase/2.json`) must remain **byte-identical**
  before and after (git-diff gate); `@Database` version stays **2** through this gate (the
  single version bump to **3** lands in Gate 3 with the rescoped v3 index cut).
- Folds in the low-priority doc fixes: 5 stale KDocs (4 DAOs "not yet registered in `@Database`
  or Koin", 1 converter "orphan converter class").
- `AGENTS.md` updated to Room 3 facts (final task of the gate).

**Gate 3 — instrumented test revival (device required).**

- Spike to re-derive the Room 3 `MigrationTestHelper` API (V5) — the 2.8.4 learnings are stale by
  design.
- Un-defer and implement `skillet-rm-08` (v2 data-preservation test) and `skillet-rm-09`
  (migration-chain test; final schema is the v3 export) in `app/src/androidTest/`.
- **Rescoped into this change (2026-09-24):** the captured index set from a parallel
  workstream (8 indexes on the 6 entities) lands here as a single **v2 → v3 schema cut**
  (`@Database` version 3; `3.json` = `2.json` + exactly those 8 indexes; explicit
   `MIGRATION_2_3`; `2.json` stays byte-identical) — new bead `skillet-g3-schema`
   (F-1 resolved 2026-09-24: all 8 indexes NON-UNIQUE, option (a); the captured uniqueness
   was structurally unachievable on the multi-row foreign-key columns per the app's own
   write path; see design D12/F-1).

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `domain-persistence`: new ADDED requirements — the cutover must be proven non-destructive
  (byte-identical v2 schema export, data intact across close/reopen on device), the app must not
  bundle a second SQLite engine, the single v3 index cut must be provable (new `3.json` = v2
  schema + exactly the 8 captured indexes, v2 rows intact across 2→3, explicit
  `MIGRATION_2_3`), and the migration chain plus v2 data preservation must be covered by
  instrumented tests (revives the deferred `skillet-rm-08`/`skillet-rm-09` scope).

## Impact

- **Source (15 files, all `:app`)**: `SkilletApp.kt`, `database/migrations/Migrations.kt`,
  `database/RecipeDatabase.kt`, `database/MeasurementConverters.kt`,
  `database/entity/{Recipe,Ingredient,Instruction,Equipment,InstructionIngredient,
  InstructionEquipment}Entity.kt`, `database/dao/{Recipe,Ingredient,Instruction,
  Equipment}Dao.kt`, `data/RecipeRepositoryImpl.kt`.
- **Build**: `gradle/libs.versions.toml`, `app/build.gradle.kts` (plugin, room3 bundle,
  `androidx.sqlite:sqlite-android`, `room3-testing` in androidTest).
- **Tests**: `app/src/androidTest/` gains the two revived migration tests plus the v3
  schema-cut test (currently only `ExampleInstrumentedTest.kt`). `app/src/test/` is untouched
  and must show no NEW failures vs. the G1-AUDIT baseline (design D10/D11).
- **Docs**: `AGENTS.md` (Room 3 facts), 5 stale KDocs folded into the Gate 2 sweep.
- **Untouched**: `:utils`, `:measurement` (no Room dependency), on-disk user data, module
  layout, DI wiring shape. (Schema version is NOT untouched: the single 2→3 cut, task 3.4/D12;
  the checked-in `2.json` export itself is untouched at every gate.)
- **Verification**: per gate — `./gradlew :app:assembleDebug`, `./gradlew test`,
  `./gradlew :app:lint` (failures judged against the G1-AUDIT baseline per design D10/D11,
  not absolute green); after Gates 1–2 — `rg "SupportSQLite" app/src` → zero and
  `git diff --stat app/schemas` → no `2.json` diff (end state after the v3 cut: `2.json`
  unchanged + new `3.json`); manual device launch after each gate (data intact);
  Gate 3 — `./gradlew :app:connectedDebugAndroidTest`.
