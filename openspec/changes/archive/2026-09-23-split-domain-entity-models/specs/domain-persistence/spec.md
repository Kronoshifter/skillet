## Purpose

Separates the app's domain recipe models from the Room entity models that persist them, defining the relational storage layout, the domain↔entity mapping boundary, and the invariants that guarantee a recipe's identity, ordering, and cross-references survive a round-trip through storage.

## ADDED Requirements

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
