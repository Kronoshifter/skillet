# Design: Expand Detekt Ruleset for Android/Compose

## Context

The Skillet app has detekt v2.0.0-alpha.6 and ktfmt v0.27.0 installed via the `add-detekt-ktfmt` change. The current `detekt.yml` uses `buildUponDefaultConfig = false` with only ~8 rules explicitly enabled. The project uses Compose with PascalCase composable functions, animation durations as literals (220, 120, 90), `WhileSubscribed(5000L)` in ViewModels, and wildcard imports throughout UI code (~12 wildcards per large screen file).

The Oracle's speculation evaluated three options: full defaults (150-300+ violations), all rules (400-600+ violations), and a hybrid approach (recommended, 50-100 violations). This design adopts the hybrid approach with phased rollout.

## Goals / Non-Goals

**Goals:**
- Enable `buildUponDefaultConfig = true` with `allRules = false` for a balanced rule surface
- Configure Compose-aware overrides so composables aren't flagged by naming rules
- Create a detekt baseline to suppress initial violations and prevent build breakage
- Run ktfmt format pass to clean wildcard imports (ktfmt-compatible, 100% safe)
- Phase 1 + Phase 2 rules only; defer Phase 3 to a future change

**Non-Goals:**
- Phase 3 rules (`ComplexMethod`, `CyclomaticComplexMethod`, `SwallowedException`, `TooGenericExceptionCaught`) — deferred
- Refactoring composables to reduce complexity
- Extracting magic numbers into named constants (optional polish, not required)
- Enabling detekt in CI or build lifecycle (remains optional tasks)
- Adding test code to detekt analysis (remains excluded)

## Decisions

### D1: Hybrid approach — `buildUponDefaultConfig = true`, `allRules = false`

**Decision:** Inherit all default rule sets but explicitly configure overrides for Compose patterns. Disable rules that are too noisy for initial adoption.

**Rationale:** Option A (full defaults) would produce 150-300+ violations — too many for a single PR. Option B (all rules) would produce 400-600+ — demoralizing and unactionable. The hybrid approach targets 50-100 violations, manageable in one change. `allRules = false` prevents the `coroutines` and `performance` rule sets from adding noise (e.g., `InjectDispatcher`, `SpreadOperator`).

**Alternatives considered:**
- Full defaults (`buildUponDefaultConfig = true`, `allRules = false`, no overrides) — rejected: too many violations, requires too much code change
- All rules (`buildUponDefaultConfig = true`, `allRules = true`) — rejected: 400-600+ violations, unactionable

### D2: `FunctionNaming` allows PascalCase for `@Composable` functions

**Decision:** Override `functionPattern` from `'[a-z][a-zA-Z0-9]*'` to `'[a-zA-Z][a-zA-Z0-9]*'` to allow PascalCase.

**Rationale:** Compose convention uses PascalCase for composable functions (`AddEditRecipeScreen`, `CookingContent`). This is the official Kotlin/Compose style guide recommendation. Allowing PascalCase breaks the general naming convention but aligns with the framework's design.

**Trade-off:** Allowing PascalCase for all functions (not just composables) is slightly more permissive than ideal. However, the project already uses PascalCase for composables, and the alternative — excluding `@Composable` functions via regex — is more complex and harder to maintain.

### D3: `MagicNumber` excludes common constants and expressions

**Decision:** Configure `ignoreNumbers: ["-1", "0", "1", "2"]` and `ignoreExpressions: true`.

**Rationale:** Animation durations (220, 120, 90), alpha values (0.2f), and state timeout values (5000) are all legitimate magic numbers in UI code. The `ignoreNumbers` list handles the most common small constants. `ignoreExpressions: true` suppresses violations inside animation expressions and composable default values where magic numbers are idiomatic.

**Trade-off:** Overly permissive `ignoreExpressions` might hide actual magic numbers in business logic. However, the project's business logic is minimal (repository layer), and the UI code is where magic numbers appear. If business logic magic numbers become a problem, `ignoreExpressions` can be tightened later.

### D4: `TopLevelPropertyNaming` allows CompositionLocal PascalCase

**Decision:** Override `pattern` from `'[a-zA-Z_][a-zA-Z0-9_]*'` to `'[a-zA-Z][a-zA-Z0-9_]*'`.

**Rationale:** `LocalSkilletBottomNavigationBarVisibility` and `LocalSkilletBottomNavigationBarState` follow the official Compose convention for CompositionLocal naming. The default pattern requires an underscore after the first character, which CompositionLocals don't use.

**Trade-off:** Allows PascalCase for all top-level properties, not just CompositionLocals. However, the project doesn't have other PascalCase top-level properties, so this is safe.

### D5: Disable `LongMethod`, `LongParameterList`, `TooManyFunctions`

**Decision:** Explicitly disable these rules with `active: false`.

**Rationale:**
- `LongMethod`: Compose composables are naturally verbose due to declarative UI structure. `AddEditRecipeScreen.kt` is 1751 lines — extracting sub-composables is a refactoring effort, not a linting fix.
- `LongParameterList`: Repository methods (`createRecipe`, `updateRecipe`) have explicit parameter lists for clarity. Composable parameters follow the same pattern.
- `TooManyFunctions`: ViewModels with multiple states and repository with many methods are intentional architecture decisions.

**Trade-off:** These rules encourage better architecture (smaller functions, fewer parameters). Disabling them doesn't discourage future refactoring — it just doesn't enforce it via linting. They can be re-enabled after a refactoring pass.

### D6: Enable `WildcardImport` and `ImportOrdering` after ktfmt pass

**Decision:** Run ktfmt format pass first, then enable these rules. Commit the ktfmt changes and the detekt config in the same change.

**Rationale:** Wildcard import cleanup is 100% ktfmt-compatible (ktfmt's `removeUnusedImports = true` handles it). Running ktfmt first ensures the codebase is already clean when detekt checks it. This avoids immediate violations.

**Trade-off:** The ktfmt pass produces a large diff. However, it's purely import cleanup — no logic changes. ktfmt is already installed and proven safe.

### D7: Commit `detekt-baseline.xml` as part of the change

**Decision:** Generate the baseline after the first detekt run with expanded rules, then commit it.

**Rationale:** Without a baseline, the initial detekt run with expanded rules would produce 50-100 violations and potentially break the build (depending on `failOnSeverity`). The baseline suppresses these known violations, allowing the rules to be active without build breakage. Violations can be fixed incrementally in future changes.

**Trade-off:** A baseline can mask real issues if not regularly reviewed. However, the baseline is a standard detekt feature and the project has no CI to enforce baseline hygiene. The baseline should be reviewed periodically.

### D8: Phase 3 rules deferred

**Decision:** Do not enable `ComplexMethod`, `CyclomaticComplexMethod`, `SwallowedException`, or `TooGenericExceptionCaught` in this change.

**Rationale:** These rules require code refactoring (extracting sub-composables, improving exception handling) that is beyond the scope of a linting configuration change. They should be enabled after a refactoring pass in a future change.

**Trade-off:** These are valuable rules that catch real issues. Deferring them means the codebase won't be checked for complexity or exception handling quality until a future change.

## Risks / Trade-offs

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| Initial detekt run produces 50-100 violations | High | `detekt-baseline.xml` suppresses all violations; build won't break |
| ktfmt pass produces a large diff | Medium | Diff is purely import cleanup — no logic changes. Review before committing. |
| `FunctionNaming` PascalCase override flags non-composable functions | Low | Project only uses PascalCase for composables; low risk of false positives |
| `MagicNumber` `ignoreExpressions` hides real issues | Low | Business logic is minimal; UI magic numbers are idiomatic |
| Baseline masks real issues over time | Medium | Baseline should be reviewed periodically; not enforced by CI |
| Phase 3 rules deferred means complexity goes unchecked | Medium | Complexity rules can be enabled after a refactoring pass in a future change |

## Migration Plan

1. **Run ktfmt format pass** — `./gradlew ktfmtFormatMain` — to clean wildcard imports
2. **Update `detekt.yml`** — rewrite with hybrid rule configuration, Compose overrides, and disabled noisy rules
3. **Update `app/build.gradle.kts`** — change `buildUponDefaultConfig = true`, add `allRules = false`, add `baseline` config
4. **Run detekt** — `./gradlew detekt` — capture violations
5. **Generate baseline** — run detekt with `--baseline` flag or use `detekt baseline` command to generate `detekt-baseline.xml`
6. **Commit baseline** — add `detekt-baseline.xml` to suppress initial violations
7. **Verify** — run `./gradlew detekt` and confirm zero violations (baseline suppresses known issues)
8. **Build** — run `./gradlew :app:assembleDebug` to confirm build still compiles

## Open Questions

_None._ All technical decisions are resolved based on the Oracle's speculation analysis.
