## Why

The Skillet app currently has no domain/use-case layer. Business logic (validation, scraping, scaling) lives in ViewModels and the Repository class, violating single-responsibility and making the codebase hard to test, mock, and reason about. This change introduces a domain layer with use cases and a repository interface, enabling testability, clean architecture, and incremental refactoring.

## What Changes

- **Extract `ScaleRecipe` use case** — Scaling logic moves from ViewModels/composables into a single domain use case. `currentServings: Int` becomes the single source of truth; `scale` is derived.
- **Introduce `RecipeRepository` interface** — The existing concrete `RecipeRepository` class becomes `RecipeRepositoryImpl`. An interface enables mocking in unit tests and DI flexibility.
- **Extract `CreateRecipe` and `ValidateRecipe` use cases** — Form validation logic (`validateForm()`) moves from `AddEditRecipeViewModel` into a domain use case. Recipe creation delegates to the use case.
- **Extract `ScrapeRecipe` use case** — Scraping orchestration moves from `AddEditRecipeViewModel` into a domain use case, making it testable without Android instrumentation.
- **Refactor scaling in UI** — `RecipeScreen` and `CookingScreen` receive `currentServings: Int` instead of `scale: Float`. Preset buttons become "set servings to X" rather than "set scale to Y".
- **Wire use cases in Koin** — Use cases registered as `single` (or `singleOf`). The koin compiler plugin (staged by user) is respected.

## Capabilities

### New Capabilities

- `domain-scaling`: `ScaleRecipe` use case with `currentServings` as single source of truth; `ScaledRecipe` result type; scaling factor derived from `targetServings / baseServings`.
- `domain-repository`: `RecipeRepository` interface defining data access contracts; `RecipeRepositoryImpl` implements it; Koin wires interface to implementation via `bind`.
- `domain-validation`: `ValidateRecipe` use case using `result-kotlin` (`Result<T, E>`) for typed validation errors; moves validation rules from `AddEditRecipeViewModel`.
- `domain-scraping`: `ScrapeRecipe` use case orchestrating `RecipeScraper` + `IngredientParser`; returns `Result<ScrapedRecipe, ScrapeError>` where `ScrapedRecipe` is a domain type (not ViewModel); ViewModel converts `ScrapedRecipe` to `RecipeState`; makes scraping testable.

### Modified Capabilities

*(None — this is a new capability addition. Existing behavior is preserved via delegation.)*

## Impact

| Area | Impact |
|------|--------|
| `data/RecipeRepository.kt` | Split into `RecipeRepository` interface + `RecipeRepositoryImpl` |
| `ui/viewmodel/*.kt` | All 4 ViewModels delegate to use cases; `RecipeViewModel` and `CookingViewModel` drop `scale: Float` from state |
| `ui/screen/recipe/RecipeScreen.kt` | Scaling controls refactor: `currentServings` instead of `scale`; preset buttons set servings |
| `ui/screen/cooking/CookingScreen.kt` | Receives `currentServings: Int` from navigation instead of `scale: Float` |
| `navigation/Route.kt` | `Route.Cooking` param changes from `scale: Float` to `currentServings: Int` |
| `navigation/SkilletNavigation.kt` | `navigateToCooking` signature changes |
| `SkilletApp.kt` | DI wiring updates; koin compiler plugin (staged) preserved |
| **New: `domain/scaling/`** | `ScaleRecipe`, `ScaledRecipe` |
| **New: `domain/repository/`** | `RecipeRepository` interface, `RecipeRepositoryImpl` |
| **New: `domain/validation/`** | `ValidateRecipe` |
| **New: `domain/scraping/`** | `ScrapeRecipe` |

**Non-goals:**
- Data-entity separation (Room-coupled models → pure domain entities) — deferred to a future change
- ANTLR parser relocation — parsing stays in `app` module
- Room schema migration — version 1 only
