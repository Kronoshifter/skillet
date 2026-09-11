# Proposal: Add Detekt & ktfmt to JVM Modules and Relocate Test Files

## Why

The Skillet app has three Gradle modules: `:app` (Android library), `:utils` (pure Kotlin JVM), and `:measurement` (pure Kotlin JVM). Detekt and ktfmt were previously added to `:app` only (see `openspec/changes/add-detekt-ktfmt`). The two JVM modules have **no static analysis or automated formatting**, creating an inconsistency in the quality pipeline.

Additionally, two test files (`ExtensionTests.kt` and `FractionTests.kt`) that test `:utils` production code (`gcd`, `roundToEighth`, `Fraction`, `fraction` extension) currently live in `:measurement/` solely because `:measurement` depends on `:utils`. Tests should live alongside the code they test.

## What Changes

### Task A — Add detekt + ktfmt to `:utils` and `:measurement`

- Apply `alias(libs.plugins.detekt)` and `alias(libs.plugins.ktfmt)` to both `utils/build.gradle.kts` and `measurement/build.gradle.kts`
- Add module-level `detekt { ... }` and `ktfmt { ... }` config blocks referencing shared root config (Option A — module-level blocks, as decided by Oracle speculation)
- Create per-module detekt baselines: `utils/detekt-baseline.xml` and `measurement/detekt-baseline.xml`
- Run detekt on both modules and populate baselines with discovered violations
- Run ktfmt on both modules to align formatting

### Task B — Relocate test files from `:measurement` → `:utils`

- Move `ExtensionTests.kt` from `measurement/src/test/kotlin/com/kronos/measurement/` to `utils/src/test/kotlin/com/kronos/utils/`
- Move `FractionTests.kt` from `measurement/src/test/kotlin/com/kronos/measurement/` to `utils/src/test/kotlin/com/kronos/utils/`
- Change package declarations from `com.kronos.measurement` to `com.kronos.utils` in both files
- Delete the original files from `measurement/`
- `MeasurementTests.kt` (896 lines) stays in `:measurement` — it tests `:measurement`'s own code

## Capabilities

### Modified Capabilities

- **code-quality-tooling** — extends detekt coverage from `:app` to `:utils` and `:measurement`
- **code-formatting** — extends ktfmt coverage from `:app` to `:utils` and `:measurement`

## Impact

**Files modified:**
- `utils/build.gradle.kts` — add detekt + ktfmt plugins and config blocks
- `measurement/build.gradle.kts` — add detekt + ktfmt plugins and config blocks

**Files created:**
- `utils/detekt-baseline.xml` — per-module detekt baseline
- `measurement/detekt-baseline.xml` — per-module detekt baseline
- `utils/src/test/kotlin/com/kronos/utils/ExtensionTests.kt` — moved from measurement/
- `utils/src/test/kotlin/com/kronos/utils/FractionTests.kt` — moved from measurement/

**Files deleted:**
- `measurement/src/test/kotlin/com/kronos/measurement/ExtensionTests.kt` — moved to utils/
- `measurement/src/test/kotlin/com/kronos/measurement/FractionTests.kt` — moved to utils/

**No production code changes.** No API changes. No behavioral changes.

## Risks

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| Detekt finds unexpected violations in JVM modules | Medium | Per-module baselines capture known issues; start with empty baselines, run detekt first, then populate |
| ktfmt reformats existing code on first run | High (cosmetic) | Idempotent and benign; aligns code to project standard |
| Test package rename breaks nothing | None | No other files reference these test classes by FQN; Kotest discovers by class name |

## Open Questions Answered

1. **Baselines: empty or pre-populated?** — Start empty, run detekt first, then populate. This gives an accurate baseline.
2. **Run ktfmt now or later?** — Run now. It's idempotent and ensures consistent formatting from day one.
3. **Replicate `failOnSeverity = Error`?** — Yes, for consistency with `:app`.
