# domain-validation Specification

## Purpose
Moves form validation logic from ViewModels into a dedicated domain use case, using `result-kotlin` for typed validation errors.

## Requirements

### Requirement: ValidateRecipe use case SHALL validate recipe form data

The `ValidateRecipe` use case SHALL accept recipe form fields and return `Result<Recipe, InvalidFormError>` using `result-kotlin`.

#### Scenario: Valid recipe passes validation
- **WHEN** `ValidateRecipe` receives a non-blank name, at least one ingredient, at least one instruction, servings > 0, and cookTime > 0
- **THEN** it returns `ok(recipe)`

#### Scenario: Blank name fails validation
- **WHEN** `ValidateRecipe` receives an empty or blank name
- **THEN** it returns `err(InvalidFormError("Name cannot be blank"))`

#### Scenario: Missing ingredients fails validation
- **WHEN** `ValidateRecipe` receives zero ingredients
- **THEN** it returns `err(InvalidFormError("At least one ingredient is required"))`

#### Scenario: Missing instructions fails validation
- **WHEN** `ValidateRecipe` receives zero instructions
- **THEN** it returns `err(InvalidFormError("At least one instruction is required"))`

#### Scenario: Zero or negative servings fails validation
- **WHEN** `ValidateRecipe` receives servings <= 0
- **THEN** it returns `err(InvalidFormError("Servings must be greater than 0"))`

#### Scenario: Zero or negative cook time fails validation
- **WHEN** `ValidateRecipe` receives cookTime <= 0
- **THEN** it returns `err(InvalidFormError("Cook time must be greater than 0"))`
