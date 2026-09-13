# domain-scaling Specification

## Purpose
Provides recipe scaling with a single source of truth (`currentServings: Int`) so scale and servings never drift apart due to floating-point arithmetic.

## Requirements

### Requirement: ScaleRecipe use case SHALL compute scaled ingredients

The `ScaleRecipe` use case SHALL accept a `Recipe` and a `targetServings: Int` and produce a `ScaledRecipe` with all ingredient measurements multiplied by the derived factor.

#### Scenario: Scale up recipe to double servings
- **WHEN** `ScaleRecipe` is invoked with a recipe of 4 servings and `targetServings = 8`
- **THEN** the factor is `2.0f` and all ingredient measurements are multiplied by `2.0f`

#### Scenario: Scale down recipe to half servings
- **WHEN** `ScaleRecipe` is invoked with a recipe of 4 servings and `targetServings = 2`
- **THEN** the factor is `0.5f` and all ingredient measurements are multiplied by `0.5f`

#### Scenario: Scale to same servings produces identity result
- **WHEN** `ScaleRecipe` is invoked with a recipe of 4 servings and `targetServings = 4`
- **THEN** the factor is `1.0f` and all ingredient measurements are unchanged

### Requirement: currentServings SHALL be the single source of truth for scaling

The UI state for scaling SHALL hold only `currentServings: Int`. The scale factor is derived as `currentServings / baseServings.toFloat()`.

#### Scenario: Preset button sets servings, not scale
- **WHEN** user taps "2x" preset button on a recipe with 4 base servings
- **THEN** `currentServings` becomes `8` (4 × 2), not `scale = 2.0f`

#### Scenario: Servings +/- buttons adjust currentServings by one
- **WHEN** user taps "+" on a recipe with 4 base servings and currentServings = 4
- **THEN** `currentServings` becomes `5` and scale derives to `5 / 4 = 1.25f`

#### Scenario: Servings cannot go below 1
- **WHEN** user taps "-" when `currentServings = 1`
- **THEN** `currentServings` remains `1` (coerced to minimum)

### Requirement: ScaledRecipe SHALL be an immutable result type

The `ScaledRecipe` type SHALL contain `scaledIngredients: List<Ingredient>`, `servings: Int`, and `factor: Float`.

#### Scenario: ScaledRecipe contains all original recipe metadata
- **WHEN** `ScaleRecipe` produces a `ScaledRecipe`
- **THEN** it includes `servings`, `factor`, and `scaledIngredients` as its only fields
