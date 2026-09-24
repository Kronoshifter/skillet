# Tasks: Room 2.8.4 → 3.0.0 Migration

Each task maps 1:1 to a bead (see "Bead" column; all beads created and wired, parented
under `skillet-m5r`; `bd dep cycles` clean, `bd ready` shows `skillet-g1-audit` first).
Gates 1–2 require no device; Gate 3 requires an emulator/device.
Verify commands: `./gradlew :app:assembleDebug`, `./gradlew test`, `./gradlew :app:lint`,
`./gradlew :app:ktfmtCheck`, `./gradlew :app:detekt` (gates compare against the G1-AUDIT
baseline snapshot — see design.md D10/R10), `./gradlew :app:connectedDebugAndroidTest`
(Gate 3 only).

## Gate 1 — Modernize on Room 2.8.4 (no device)

- [ ] **1.1** `G1-AUDIT` KSP baseline audit + pre-migration baseline snapshot — confirm
  KSP 2.3.10 ↔ Kotlin 2.4.10 pairing drives the current Room KSP path cleanly; record
  baseline + any warnings in bead comment; ALSO snapshot the pre-migration baseline
  failure sets (file:rule lists) of `./gradlew :app:ktfmtCheck`, `./gradlew :app:detekt`,
  `./gradlew :app:lint`, and `./gradlew test` in a bead comment — the 1.6/2.5 gates are
  baseline-relative and compare against this snapshot (design D10).
  Bead: `skillet-g1-audit` · deps: —
- [ ] **1.2** `G1-MIGRATIONS` convert `MIGRATION_1_2` (`database/migrations/Migrations.kt`)
  to `migrate(connection: SQLiteConnection)` with `import androidx.sqlite.SQLiteConnection`
  + `import androidx.sqlite.execSQL`; DDL strings byte-identical.
  Bead: `skillet-g1-migr` · deps: 1.1
- [ ] **1.3** `G1-CALLBACK` convert `fkPragmaCallback` (`SkilletApp.kt:53-58`) to
  `onOpen(connection: SQLiteConnection)`; keep FK-pragma comment. Fallback per design R4:
  if the 2.8.4 overload is missing, defer callback to Gate 2 and note it.
  Bead: `skillet-g1-cb` · deps: 1.1
- [ ] **1.4** `G1-TX` convert `database.withTransaction` (`RecipeRepositoryImpl.kt:46-63`)
  to `useWriterConnection { connection -> connection.immediateTransaction { … } }`
  (`import androidx.room.useWriterConnection`, `import androidx.room.immediateTransaction`);
  transaction body unchanged.   Bead: `skillet-g1-tx` · deps: 1.1
- [ ] **1.5** `G1-DRV` add `androidx.sqlite:sqlite-android:2.7.1` to catalog +
  `implementation` (align with Room's transitive `androidx.sqlite` core version); builder in
  `SkilletApp.kt:60-68`: `.setQueryCoroutineContext(Dispatchers.IO)` then
  `.setDriver(AndroidSQLiteDriver())` **LAST** (disables compat mode).   Bead: `skillet-g1-drv` · deps: 1.2, 1.3, 1.4
- [ ] **1.6** `G1-GATE` full verification (baseline-relative; snapshot in the
  `skillet-g1-audit` comment): 1) `./gradlew :app:assembleDebug` green; 2) `./gradlew test`
  green with no new failures vs. the G1-AUDIT baseline snapshot; 3) `./gradlew :app:lint`
  green with no new findings vs. the snapshot; 4) `./gradlew :app:ktfmtCheck` and
  `./gradlew :app:detekt` green on every file the migration touched (newly touched files
  must be ktfmt-clean; pre-existing baseline failures in untouched files may remain);
  5) `rg "SupportSQLite" app/src` → 0 matches; 6) `git diff --stat app/schemas` → empty
  (schema 2.json byte-identical).
  Bead: `skillet-g1-gate` · deps: 1.5

## Gate 2 — Swap to Room 3.0.0 (no device)

- [ ] **2.1** `G2-VERIFY` resolve before any edit (record in bead comment): V1 Room 3 Gradle
  plugin id; V2 `room3-ktx` existence; `room3-testing` artifact name; `@Upsert` support in
  `room3-compiler`; KSP version requirement.   Bead: `skillet-g2-verify` · deps: 1.6
- [ ] **2.2** `G2-DEPS` catalog + build swap: `room3 = "3.0.0"` + `androidx.room3:room3-runtime`
  / `room3-compiler` (+ ktx if it exists); plugin swap; `ksp(libs.androidx.room3.compiler)`;
  bundle swap; `androidTestImplementation` test-artifact swap; conditional KSP bump per 1.1/2.1.
  Bead: `skillet-g2-deps` · deps: 2.1
- [ ] **2.3** `G2-IMPORTS` sweep 15 files `androidx.room` → `androidx.room3` (6 entities, 4 DAOs,
  `RecipeDatabase`, `Migrations`, `SkilletApp`, `MeasurementConverters`,
  `RecipeRepositoryImpl`); fix 5 stale KDocs (4 DAOs: "not yet registered in `@Database` or
  Koin…"; `MeasurementConverters.kt:17`: "orphan converter class…").
  Bead: `skillet-g2-imports` · deps: 2.2
- [ ] **2.4** `G2-SIG` suspend + rename signatures: `migrate` → `suspend` +
  `import androidx.sqlite.async.executeSQL` (call `executeSQL`); `onOpen` → `suspend`;
  `useWriterConnection/immediateTransaction` → `withWriteTransaction`;
  `@TypeConverter` → `@ColumnTypeConverter` (2 sites, `MeasurementConverters.kt`);
  `@TypeConverters` → `@ColumnTypeConverters` (`RecipeDatabase.kt`).   Bead: `skillet-g2-sig` · deps: 2.3
- [ ] **2.5** `G2-GATE` full verification (baseline-relative; snapshot in the
  `skillet-g1-audit` comment): 1) `./gradlew :app:assembleDebug` green; 2) `./gradlew test`
  green with no new failures vs. the G1-AUDIT baseline snapshot; 3) `./gradlew :app:lint`
  green with no new findings vs. the snapshot; 4) `./gradlew :app:ktfmtCheck` and
  `./gradlew :app:detekt` green on every file the migration touched (newly touched files
  must be ktfmt-clean; pre-existing baseline failures in untouched files may remain);
  5) `rg "SupportSQLite" app/src` → 0 matches; 6) `rg "^import androidx\.room\." app/src` →
  0 matches (no non-room3 Room imports); 7) `git diff --stat app/schemas` → empty
  (schema 2.json byte-identical).
  Bead: `skillet-g2-gate` · deps: 2.4
- [ ] **2.6** `G2-DOCS` update `AGENTS.md`: Room 3.0.0, room3 plugin/artifacts,
  `AndroidSQLiteDriver`, suspend migration/callback API, migration-test notes.
  Bead: `skillet-g2-docs` · deps: 2.5

## Gate 3 — Instrumented migration tests (device required)

- [ ] **3.1** `G3-SPIKE` re-derive Room 3 migration-test mechanics (artifact, helper FQN,
  schema-directory argument, `migrate(1)` usage, suspend-ness, V4 schema-JSON delivery to
  test); minimal scratch smoke test; record mechanics in bead comment. If unreachable on
  device, STOP + flag human.   Bead: `skillet-g3-spike` · deps: 2.6 (+ `blocks:skillet-rm-08`)
- [ ] **3.2** `G3-RM08` un-defer `skillet-rm-08` + wire edges (spike blocks rm-08); execute
  v2 data-preservation test: open v2 `RecipeDatabase`, seed rows via DAOs, close, reopen,
  assert every row intact. Bead: `skillet-rm-08` (existing) · deps: 3.1
- [ ] **3.3** `G3-RM09` un-defer `skillet-rm-09` + wire edges (rm-08 blocks rm-09); execute
  v1→v2 test: `migrate(1)` from `1.json`, seed v1 recipe row, open DB so `MIGRATION_1_2`
  runs, assert schema matches `2.json` + seeded v1 row gone (destructive contract).
  Bead: `skillet-rm-09` (existing) · deps: 3.2
- [ ] **3.4** `G3-GATE` full instrumented suite green
  (`./gradlew :app:connectedDebugAndroidTest`, includes existing `ExampleInstrumentedTest`).
  Bead: `skillet-g3-gate` · deps: 3.3

## Close-out

- [ ] Close all Gate 1–3 beads as they complete; update `skillet-m5r` description with the
  change name (`room3-migration`); close `skillet-m5r` when 3.4 is green.
