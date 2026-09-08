# Proposal: Expand Detekt Ruleset for Android/Compose

## Why

The initial detekt configuration (`add-detekt-ktfmt` change) started with a minimal rule set to avoid build breakage on first run. Now that the tooling is in place and ktfmt has been applied, the codebase is ready for a broader static analysis surface. The current ~8 rules miss important quality checks — wildcard imports, magic numbers, unused private members, and naming violations are all unmonitored. Expanding the ruleset with Compose-aware overrides catches real issues while respecting the project's Compose patterns.

## What Changes

- **Enable `buildUponDefaultConfig = true`** in `app/build.gradle.kts` to inherit all default detekt rule sets
- **Rewrite `detekt.yml`** with phased rule configuration:
  - Phase 1 (safe): `WildcardImport`, `ImportOrdering`, `UnusedPrivateFunction`, `UnusedPrivateProperty`
  - Phase 2 (Compose-friendly overrides): `FunctionNaming` (PascalCase for composables), `MagicNumber` (ignore common constants and expressions), `TopLevelPropertyNaming` (CompositionLocal PascalCase), disable `LongMethod`/`LongParameterList`/`TooManyFunctions`
  - Phase 3 (post-baseline): `ComplexMethod`, `CyclomaticComplexMethod`, `SwallowedException`, `TooGenericExceptionCaught` — deferred to a later change
- **Create `detekt-baseline.xml`** to suppress initial violations and prevent build breakage during rollout
- **Run ktfmt format pass** to clean wildcard imports and enforce import ordering before detekt validation
- **Configure baseline path** in `app/build.gradle.kts` detekt block

## Capabilities

_None_ — this is pure tooling infrastructure. No behavioral requirements change. Consistent with the parent `add-detekt-ktfmt` change which also used `skip_specs: true`.

## Impact

**Files modified:**
- `detekt.yml` — **major rewrite**: expand rule sets, add Compose-aware overrides, adjust severity thresholds
- `app/build.gradle.kts` — change `buildUponDefaultConfig` to `true`, add `baseline` configuration, add `allRules = false`
- `detekt-baseline.xml` — **new file**: generated after first detekt run with expanded rules, committed to suppress initial violations
- `app/src/main/java/com/kronos/skilletapp/ui/screen/*.kt` — wildcard import cleanup (ktfmt-compatible)
- `app/src/main/java/com/kronos/skilletapp/ui/component/*.kt` — wildcard import cleanup (ktfmt-compatible)

**No source code logic changes.** ktfmt handles import cleanup; detekt config changes only affect static analysis output.

**Dependencies:** None — builds on the existing `add-detekt-ktfmt` change.
