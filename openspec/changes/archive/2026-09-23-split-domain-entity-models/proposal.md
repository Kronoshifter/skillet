## Why

The persistence layer conflates two responsibilities that have diverged: Room `@Entity` models that double as the app's domain models, and JSON-blob storage that hides the recipe's true structure behind silent-failure converters. That conflation blocks relational querying, forces the domain layer to regenerate identities on every upsert, and lets the scraping domain use case import a `ui`-package type (`RecipeState`) — an inversion of layering that keeps the domain untestable in the shape it should be. This change separates domain models from entity models and reworks storage to relational tables.

## What Changes

- **New:** Separate pure domain models (`Recipe`, `Ingredient`, `Instruction`, `Equipment`, `RecipeTime`, `RecipeSource`) from Room entity models; add an entity↔domain **mapper** (registered in Koin).
- **New:** Relational storage — `recipe`, `ingredient`, `instruction`, `equipment` tables plus `instruction_ingredients` / `instruction_equipment` join tables, each with explicit `position` columns. **BREAKING:** destructive migration v1 → v2 (drops the `recipe` table; existing rows are discarded).
- **New:** A single `Measurement` Room type-converter (Float quantity + serialized `MeasurementUnit`); **delete** `RecipeConverters` and every JSON list-of-T ⇄ String converter.
- **Modified:** `RecipeRepository.createRecipe` / `updateRecipe` collapse from 14 flat arguments to taking a domain `Recipe`; the impl composes entity flows and writes atomically in a single `@Transaction`.
- **Modified:** IDs are generated **once** at domain-model creation and preserved across upserts (no UUID regeneration).
- **Modified:** `ScrapeRecipe` returns the domain `ScrapedRecipe` DTO (not a `ui` type); the **domain package must never import `ui`**.
- **Modified:** `RecipeState` is extracted from `AddEditRecipeViewModel.kt` and renamed `RecipeFormState` to its own file.

## Capabilities

### New Capabilities
- `domain-persistence`: Domain-model ↔ Room-entity separation, the entity↔domain mapper, the relational schema (tables + join tables + position columns), the `Measurement` converter, the v1 → v2 destructive migration, and the persistence invariants (ID preservation, write atomicity, ordered reads, N–M ingredient identity).

### Modified Capabilities
- `domain-repository`: `createRecipe` / `updateRecipe` signatures collapse to a domain `Recipe`; the impl composes entity flows and upserts atomically instead of a single `RecipeDao.upsert`.
- `domain-scraping`: `ScrapeRecipe` returns `ScrapedRecipe` (code aligned to the existing spec), the domain layer gains an explicit no-`ui`-import constraint, and `RecipeState` is renamed `RecipeFormState`.

## Impact

- `model/` — split into domain models (kept, field shapes unchanged) vs new Room entity models; `RecipeConverters` deleted.
- `database/` — `RecipeDatabase` v1 → v2, one DAO per table (plus join-table queries), a single `Measurement` converter.
- `data/` — `RecipeRepository` interface + `RecipeRepositoryImpl` (signature collapse, entity-flow composition, `@Transaction` write).
- `domain/scraping/` — `ScrapeRecipe` return-type fix; domain no longer imports `ui`.
- `ui/viewmodel/AddEditRecipeViewModel.kt` — `RecipeState` → `RecipeFormState` extraction/rename; convert `ScrapedRecipe` → `RecipeFormState`.
- `SkilletApp.kt` — Koin: register mapper, multi-table DB, per-table DAOs.
- `parser/`, `domain/scaling/`, `domain/validation/` — no field-shape changes (domain models keep today's names/fields).
- `app/schemas/` — new v2 schema JSON generated; v1 dropped.
