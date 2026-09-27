# domain-persistence Specification

## Purpose
Defines how the Room database evolves its schema across versions: the migration chain that connects every historical version to the current one, the guarantee that a missing migration step fails loudly rather than silently wiping user data, and the documented blind spot of auto-generated migrations for data serialized inside TEXT columns.

## Requirements

### Requirement: The migration chain SHALL be complete from version 1 to the current version

The database SHALL register a migration path that connects version 1 to the current schema version with no gaps. Every step in the chain SHALL be an explicit, registered migration (custom or auto-generated). The v1 → v2 step SHALL be an explicit migration, not an implicit or fallback behavior.

#### Scenario: A v1 database migrates forward
- **WHEN** a database file at version 1 is opened by the current app
- **THEN** the registered migration path is applied step by step to reach the current version, with no reliance on a destructive fallback

#### Scenario: A gap in the chain fails loudly
- **WHEN** a database file is opened at a version for which no registered migration path exists
- **THEN** opening the database throws an error (a loud launch failure) rather than silently deleting the database file

### Requirement: The destructive fallback SHALL NOT be registered

The Room database builder SHALL NOT enable `fallbackToDestructiveMigration`. The only handling of a missing migration is the loud failure defined by the complete-chain requirement above.

#### Scenario: An existing v2 database opens without data loss
- **WHEN** a database file at the current version (v2) with existing rows is opened
- **THEN** the database opens cleanly and every previously stored row is intact

### Requirement: Future schema changes SHALL be handled by registered migrations

The `@Database` SHALL declare `autoMigrations` so that future schema changes Room can resolve are applied by generated migrations, and so that any future change Room cannot resolve produces the loud failure defined above rather than a destructive wipe.

#### Scenario: A resolvable future schema bump
- **WHEN** a future version bump is a change Room can auto-resolve (add table/column/index, column rename)
- **THEN** Room generates and applies the migration, and no destructive fallback is consulted

### Requirement: autoMigrations SHALL NOT be relied upon to migrate data inside TEXT columns

`autoMigrations` computes differences between schema versions and SHALL NOT be relied upon to migrate the *content* of data stored in a TEXT column (for example, the serialized `Measurement` value). A change to a type converter's serialized format changes no schema column, so `autoMigrations` generates nothing for it. Any such change is a data migration and SHALL ship an explicit migration step.

#### Scenario: Changing a converter's serialized format
- **WHEN** the serialized form of a value stored in a TEXT column is changed (for example, the `Measurement` JSON shape)
- **THEN** an explicit migration step is required to rewrite the stored values; relying on `autoMigrations` alone leaves the old values in place and potentially unreadable

### Requirement: Domain models and Room entities SHALL be separate types

The persistence layer SHALL maintain distinct Room `@Entity` types for stored rows and SHALL NOT annotate the shared domain models (`Recipe`, `Ingredient`, `Instruction`, `Equipment`) as `@Entity`. A mapper SHALL convert between the domain models and the entity models.

#### Scenario: Domain models carry no Room annotations
- **WHEN** the domain `Recipe`, `Ingredient`, `Instruction`, and `Equipment` types are defined
- **THEN** none of them is annotated `@Entity`, and separate entity types exist for the stored rows

#### Scenario: Mapper round-trips a recipe graph
- **WHEN** a domain `Recipe` is mapped to entities and those entities are mapped back
- **THEN** the resulting domain `Recipe` is equivalent to the input (same fields, same child lists, same ordering)

### Requirement: Recipes SHALL be stored relationally

Storage SHALL persist `recipe`, `ingredient`, `instruction`, and `equipment` as separate tables. The instruction→ingredient and instruction→equipment relationships SHALL be stored in join tables that carry an explicit `position` column.

Every reference column SHALL be declared with a Room `@ForeignKey` constraint carrying `onDelete = ForeignKey.CASCADE`: child tables (`ingredient`, `instruction`, `equipment`) SHALL reference `recipe(id)` via `recipe_id`, and join tables (`instruction_ingredient`, `instruction_equipment`) SHALL reference both `instruction(id)` via `instruction_id` and `ingredient(id)` / `equipment(id)` via their child-id columns. FK enforcement SHALL be enabled at the database level (SQLite `foreign_keys` pragma set `ON` per connection) so the constraints are actually enforced.

#### Scenario: A recipe persists into its tables
- **WHEN** a domain `Recipe` with ingredients, instructions, and equipment is upserted
- **THEN** a recipe row plus ingredient, instruction, and equipment rows are stored, and the instruction-to-ingredient and instruction-to-equipment links are stored in the join tables

#### Scenario: Reads restore stored order
- **WHEN** a stored recipe is read back
- **THEN** ingredients, instructions, and equipment are ordered by their stored position, and each instruction's referenced ingredients and equipment are ordered by their stored position

#### Scenario: Reference columns are constrained and enforced
- **WHEN** the v2 relational schema is inspected
- **THEN** every reference column carries a `@ForeignKey` with `onDelete = CASCADE`, and FK enforcement is enabled so a dangling reference cannot be persisted

### Requirement: Ingredient identity SHALL be preserved across instructions

An ingredient referenced by more than one instruction SHALL be stored once and SHALL keep a stable identity; the join tables SHALL reference that identity rather than duplicate the ingredient.

#### Scenario: A shared ingredient is stored once
- **WHEN** the same ingredient (by id) is referenced by two different instructions
- **THEN** exactly one ingredient row is stored and both instructions resolve to it through the join table

### Requirement: Stable identifiers SHALL be generated once and preserved on upsert

Identifiers for a recipe and its child rows SHALL be assigned at domain-model creation and SHALL NOT be regenerated when the same model is upserted again.

#### Scenario: Upsert preserves existing IDs
- **WHEN** a recipe that already has an ID (and child IDs) is upserted
- **THEN** the recipe and its child rows retain their existing IDs and no new identifiers are minted

### Requirement: A recipe write SHALL be atomic

Persisting or updating a recipe together with all of its child rows SHALL commit as a single transaction so a failure does not leave a partially written graph.

#### Scenario: A failed write leaves no partial graph
- **WHEN** writing a recipe's child rows fails partway through
- **THEN** no new or changed rows for that recipe are persisted and the previously stored graph is intact

### Requirement: Measurement SHALL be the only converted type

Persistence SHALL provide a type converter for `Measurement` (quantity plus unit) and SHALL NOT depend on JSON list-of-T ⇄ String converters for the recipe's ingredient, instruction, or equipment collections.

#### Scenario: A measurement with a custom unit round-trips
- **WHEN** an ingredient carries a `Measurement` whose unit is a named or custom unit
- **THEN** the converter stores and restores both the quantity and the unit exactly

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
