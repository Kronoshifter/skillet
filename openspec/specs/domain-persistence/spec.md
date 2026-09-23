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
