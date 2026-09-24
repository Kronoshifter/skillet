# domain-persistence Delta: room3-migration

## ADDED Requirements

### Requirement: The Room 3 cutover SHALL NOT change the on-disk schema except the single v3 index cut

Upgrading the persistence layer from Room 2.8.4 to Room 3.0.0 SHALL NOT change the database
schema other than the single rescoped v2 → v3 index cut. The cutover itself (Gates 1–2) is a
library-surface change: it SHALL NOT bump the `@Database` version or add indexes. The exported
v2 schema file (`app/schemas/com.kronos.skilletapp.database.RecipeDatabase/2.json`) SHALL remain
byte-identical throughout the change. The only schema change is the v3 cut (task 3.4): the
`@Database` version SHALL end at 3, and the exported v3 schema (`3.json`) SHALL be the v2 schema
plus exactly the 8 captured indexes and nothing else.

#### Scenario: Schema export after the Gates 1–2 cutover
- **WHEN** the upgraded persistence layer (Gates 1–2 complete, before the v3 cut) exports the database schema
- **THEN** the v2 schema file is byte-identical to the pre-cutover export and the database version is still 2

#### Scenario: Schema export after the v3 cut
- **WHEN** the v3 index cut (task 3.4) has been applied and the schema is exported
- **THEN** the v2 schema file is still byte-identical, the database version is 3, and the exported v3 schema is the v2 schema plus exactly the 8 captured indexes

### Requirement: The app SHALL NOT bundle a second SQLite engine

The database SHALL run on the platform-provided Android SQLite driver. The application SHALL NOT
link a bundled or native SQLite engine; the migration SHALL NOT add any SQLite engine to the APK
beyond the one already provided by the Android platform.

#### Scenario: The platform driver is selected
- **WHEN** the database is built in the application
- **THEN** the driver is the platform-provided Android driver (`androidx.sqlite:sqlite-android` + its transitive `androidx.sqlite` core) and no bundled SQLite ENGINE (e.g. `androidx.sqlite:sqlite-bundled`) is on the runtime classpath; the APK adds no SQLite native library of its own

### Requirement: Existing data SHALL survive the Room 3 cutover on a real device

The cutover SHALL be verified on-device by an instrumented test that opens a v2 database, stores
rows, closes the database, reopens it, and asserts every stored row is intact. No database file
SHALL be checked into the repository as test fixture.

#### Scenario: Reopening preserves stored rows
- **WHEN** an instrumented test writes a recipe graph (recipe plus child and join rows) to a v2 database, closes it, and reopens it
- **THEN** every stored row is intact and the recipe graph reads back equivalent to what was written

### Requirement: The registered migration chain SHALL be verified by an instrumented test

An instrumented test SHALL materialize a version-1 database from the checked-in v1 schema, apply
the full registered migration chain (v1 → v2 → v3, with the v3 index cut from task 3.4 in
place), and assert that the resulting schema matches the checked-in v3 schema and that any
seeded v1 rows are gone (the v1→v2 step is destructive by design). The test SHALL use the Room
testing migration helper, not a checked-in database file.

#### Scenario: A v1 database migrates on device
- **WHEN** an instrumented test materializes a v1 database (seeded with v1-shaped rows) and applies the registered migration chain
- **THEN** the database reaches version 3 with a schema matching the checked-in v3 export and no seeded v1 rows remain

### Requirement: The v3 schema cut SHALL add exactly the captured indexes

The rescoped v2 → v3 schema cut (task 3.4) SHALL add exactly the 8 captured indexes — one per
captured entity column: `recipe.name`, `ingredient.recipe_id`, `instruction.recipe_id`,
`equipment.recipe_id`, `instruction_equipment.instruction_id`,
`instruction_equipment.equipment_id`, `instruction_ingredient.instruction_id`,
`instruction_ingredient.ingredient_id` — and SHALL NOT alter any table, column, or constraint
beyond those indexes. The cut SHALL be an explicit `MIGRATION_2_3` registered on the database
builder (auto-migration is NOT used), and the v3 schema export SHALL be KSP-stable across
consecutive builds. User data present at v2 SHALL be intact after the cut. All 8 captured indexes SHALL be
NON-UNIQUE (F-1 resolved 2026-09-24, design D12: the captured `unique = true` set was
structurally unachievable on the multi-row foreign-key columns — 7 of 8 — per the app's own
write path). `recipe.name` uniqueness is intentionally NOT enforced by the schema and is
deferred as a separate domain decision.

#### Scenario: A v2 database with data migrates to v3 on device
- **WHEN** an instrumented test seeds a v2 database with a recipe graph and opens it so the registered chain applies the v3 cut
- **THEN** every v2 row is intact, the schema matches the checked-in v3 export, and all 8 captured indexes are present

#### Scenario: The v3 export is KSP-stable
- **WHEN** the schema is exported by KSP across two consecutive builds after the v3 cut
- **THEN** both exports are byte-identical (`3.json` is stable)
