## Purpose

Defines a repository interface that abstracts data access from domain logic, enabling testability and DI flexibility.

## ADDED Requirements

### Requirement: RecipeRepository interface SHALL define all data access contracts

The `RecipeRepository` interface SHALL declare methods for CRUD operations on recipes, matching the current `RecipeRepository` class methods.

#### Scenario: Interface exposes fetch, observe, upsert, create, update

- **WHEN** the `RecipeRepository` interface exists
- **THEN** it declares: `fetchRecipe(id)`, `observeRecipe(id)`, `fetchRecipes()`, `observeRecipes()`, `upsert(recipe)`, `createRecipe(...)`, `updateRecipe(...)`

### Requirement: RecipeRepositoryImpl SHALL implement the interface

The `RecipeRepositoryImpl` class SHALL implement `RecipeRepository` and delegate all methods to the Room `RecipeDao`.

#### Scenario: Implementation delegates to DAO
- **WHEN** `RecipeRepositoryImpl.fetchRecipe(id)` is called
- **THEN** it delegates to `dao.getById(id)` and returns the result

#### Scenario: Implementation delegates createRecipe to upsert
- **WHEN** `RecipeRepositoryImpl.createRecipe(...)` is called with parsed arguments
- **THEN** it constructs a `Recipe` entity, calls `upsert(recipe)`, and returns the generated ID

### Requirement: Koin SHALL bind interface to implementation

Koin DI SHALL bind `RecipeRepository::class` to `RecipeRepositoryImpl` using the `bind` DSL.

#### Scenario: Koin resolves RecipeRepository to RecipeRepositoryImpl
- **WHEN** a component requests `RecipeRepository` from Koin
- **THEN** it receives an instance of `RecipeRepositoryImpl`

#### Scenario: Implementation is created at start
- **WHEN** Koin starts the application
- **THEN** `RecipeRepositoryImpl` is instantiated with `createdAtStart()`
