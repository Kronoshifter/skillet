## Why

`SkilletApp.kt` currently opts into Room's `fallbackToDestructiveMigration(true)` and registers **no** `Migration` objects and **no** `autoMigrations`. Today that is harmless, but it means the next schema bump Room cannot auto-resolve will **silently delete the entire `recipes.db` file**. The app now actively scrapes real recipes into the dev device's v2 database, so "no production data" is no longer true: the failure mode this change guards against is total, silent, unrecoverable data loss on an ordinary version bump.

## What Changes

- **Modified:** `RecipeDatabase`'s `@Database` gains an `autoMigrations` list (empty for now) so every *future* schema bump Room cannot auto-resolve fails loudly instead of falling back to a destructive wipe. **No schema version bump** — this change must not change the schema (it stays v2).
- **New:** An explicit, honest **destructive** `Migration(1, 2)` records the v1 → v2 jump that was already shipped, keeping the migration chain unbroken from version 1 to the current version 2. (No v1 database with real data survives, and the old v1 serialization format is deleted, so nothing is preserved — the migration is a faithful record of the wipe that already happened.)
- **Removed:** `SkilletApp.kt`'s `.fallbackToDestructiveMigration(true)`. With a complete chain registered, the fallback is unnecessary, and removing it turns "silent wipe" into "loud launch crash" for any future gap.
- **New:** The project's first database migration tests, running **on-device** (`app/src/androidTest/`) via `androidx.room:room-testing`'s `MigrationTestHelper`, driven by the checked-in schema JSONs in `app/schemas/`. An **instrumented smoke test first** proves Room opens on-device and `MigrationTestHelper` can read the schema directory (the plan's one hard unknown). The originally planned JVM vehicle (`Room.testing()` + `androidx.sqlite` driver on the unit-test classpath) was definitively ruled out after a 3-round dependency-resolution study — AGP 9.3.1 pins the unit-test classpath to the Android platform, making the driver's KMP `jvm` variant unreachable (see `design.md`, Decision 1).
- **Modified:** `AGENTS.md` — correct two stale schema claims ("version 1 only" / "migrations are not yet implemented").

## Capabilities

### Modified Capabilities
- `domain-persistence`: add migration-chain behavior — a complete, unbroken migration path from version 1 to the current version, loud-failure (never a silent wipe) on a missing step, the prohibition on the destructive fallback, and the documented blind spot of `autoMigrations` for data serialized inside TEXT columns.

## Impact

- `app/.../SkilletApp.kt` — remove `.fallbackToDestructiveMigration(true)`; register `Migration(1, 2)` on the Room builder.
- `app/.../database/RecipeDatabase.kt` — add `autoMigrations` to `@Database` (version stays 2).
- `app/.../database/migrations/` (new) — `Migration(1, 2)` and, going forward, the home for per-version migrations.
- `app/build.gradle.kts` + `gradle/libs.versions.toml` — new `androidTestImplementation` dependency: `androidx.room:room-testing` (same 2.8.4 version as `room`).
- `app/src/androidTest/...` (new) — first migration tests (instrumented, on-device).
- `AGENTS.md` — fix the two stale schema claims.
- **Everything else** (ViewModels, repository, domain, DAOs, UI, entities, schema JSONs) — **untouched**. No schema change, so existing v2 data on the dev device is untouched.
