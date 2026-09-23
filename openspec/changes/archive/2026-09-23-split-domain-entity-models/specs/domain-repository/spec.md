## MODIFIED Requirements

### Requirement: RecipeRepository interface SHALL define all data access contracts

The `RecipeRepository` interface SHALL declare methods for CRUD operations on recipes. `createRecipe` and `updateRecipe` SHALL accept a domain `Recipe` as their payload rather than a set of individual flat arguments.

#### Scenario: Interface exposes fetch, observe, upsert, create, update

- **WHEN** the `RecipeRepository` interface exists
- **THEN** it declares: `fetchRecipe(id)`, `observeRecipe(id)`, `fetchRecipes()`, `observeRecipes()`, `upsert(recipe)`, `createRecipe(recipe)`, `updateRecipe(id, recipe)`, where the create and update payloads are a domain `Recipe`

### Requirement: RecipeRepositoryImpl SHALL implement the interface

The `RecipeRepositoryImpl` class SHALL implement `RecipeRepository`. Reading SHALL delegate to the per-table DAOs, load the relational rows (recipe plus ingredient, instruction, equipment, and join rows), and assemble them into a domain `Recipe`; writing SHALL map a domain `Recipe` to its entities and persist the graph atomically in a single transaction.

#### Scenario: Implementation delegates to DAO
- **WHEN** `RecipeRepositoryImpl.fetchRecipe(id)` is called
- **THEN** it delegates to the per-table DAOs, loads the recipe row and its ordered child rows, and assembles them into a domain `Recipe`

#### Scenario: Implementation delegates createRecipe to upsert
- **WHEN** `RecipeRepositoryImpl.createRecipe(recipe)` is called with a domain `Recipe`
- **THEN** it maps the graph to entities, persists them atomically (upsert within a single transaction), and returns the recipe's existing ID

#### Scenario: updateRecipe replaces the stored graph atomically
- **WHEN** `RecipeRepositoryImpl.updateRecipe(id, recipe)` is called
- **THEN** it replaces the recipe's stored child rows within a single transaction and keeps the recipe's existing ID
