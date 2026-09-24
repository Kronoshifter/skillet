# domain-persistence Delta: room3-migration

## ADDED Requirements

### Requirement: The Room 3 cutover SHALL NOT change the on-disk schema

Upgrading the persistence layer from Room 2.8.4 to Room 3.0.0 SHALL NOT change the database
schema. The exported v2 schema file (`app/schemas/com.kronos.skilletapp.database.RecipeDatabase/2.json`)
SHALL remain byte-identical before and after the cutover, and the `@Database` version SHALL remain 2.
The cutover is a library-surface change, not a schema change.

#### Scenario: Schema export after the cutover
- **WHEN** the upgraded persistence layer exports the database schema for the unchanged `@Database`
- **THEN** the v2 schema file is byte-identical to the pre-cutover export and the database version is still 2

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

### Requirement: The v1-to-v2 migration chain SHALL be verified by an instrumented test

An instrumented test SHALL materialize a version-1 database from the checked-in v1 schema, apply
the registered migration chain to version 2, and assert that the resulting schema matches the
checked-in v2 schema and that any seeded v1 rows are gone. The test SHALL use the Room testing
migration helper, not a checked-in database file.

#### Scenario: A v1 database migrates on device
- **WHEN** an instrumented test materializes a v1 database (seeded with v1-shaped rows) and applies the registered migration chain
- **THEN** the database reaches version 2 with a schema matching the checked-in v2 export and no seeded v1 rows remain
