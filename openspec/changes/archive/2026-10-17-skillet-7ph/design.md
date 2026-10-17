# Design: skillet-7ph — CI pipeline + Fix Band + test quarantine

## Locked Gate-1 decisions (human, 2026-10-14) — do NOT re-litigate

- **D1 = B (fix-then-full).** CI runs `detektMain` (full/type-resolution mode) on all three modules and requires **overall 0 findings**. A **Fix Band** (in scope here) resolves the pre-existing out-of-scope findings to make that achievable.
- **D2 = Q-A (Kotest quarantine).** Quarantine the network-dependent `RecipeScrapingTests` via a default-off property gate (no build-file test filtering). The 6.2.3 mechanism is specified in §2 — note there is **no literal `@IgnoredWhen` annotation in the Kotest 6.2.3 API**; `@DisabledIf(Condition::class)` is the 6.x form of the locked Q-A semantics.

Environment facts (verified 2026-10-14): AGP 9.3.1, Gradle 9.7.1 (wrapper), Kotlin 2.4, Kotest 6.2.3, detekt 2.0.0-alpha.6 (via the `detekt` Gradle plugin), ktfmt plugin on all three modules, shared `$rootDir/detekt.yml`, root project name `SkilletApp`, modules `:app` (android.application), `:utils` (kotlin.jvm), `:measurement` (kotlin.jvm, depends on `:utils`).

## 1. CI pipeline (tasks 5.1–5.2 → beads `skillet-7ph.7`, `skillet-7ph.8`)

### File and triggers

New `.github/workflows/ci.yml`. `on: push` (branch `main`) + `on: pull_request`.

### Jobs (2-job split)

- **`jvm`** — no Android SDK required: `:utils` + `:measurement` gates.
- **`app`** — Android required: `:app` gates.

Rationale: isolates Android-SDK setup from the fast JVM lane; JVM-module failures never depend on the SDK, and the SDK job's runtime is bounded by the Android-only work.

### Common steps (both jobs)

1. `actions/checkout@v4`
2. `actions/setup-java@v4` — temurin, **JDK 21** (AGP 9.3.1 + Gradle 9.7.1 require JDK 17+; 21 LTS is the specified choice — memory #176)
3. `actions/setup-gradle@v4` — wrapper + dependency caching
4. The gate command (below) — the exit code IS the gate.

The `app` job additionally installs the Android platform before the gate: GitHub ubuntu runners preinstall the Android SDK (`sdkmanager` on PATH); run `yes | sdkmanager --licenses` then `sdkmanager --install "platforms;android-37" "platform-tools"` (compileSdk = 37 per `app/build.gradle.kts`).

### Gate commands

- `jvm` job: `./gradlew :utils:ktfmtCheck :measurement:ktfmtCheck :utils:detektMain :measurement:detektMain :utils:test :measurement:test`
- `app` job: `./gradlew :app:ktfmtCheck :app:detektMain :app:testDebugUnitTest`

Notes:

- `:app:detektMain` delegates to the `detektDebug` + `detektRelease` variant tasks (reports at `app/build/reports/detekt/{debug,release}.xml`); `:utils`/`:measurement` write `build/reports/detekt/main.xml`. All reports are checkstyle-format `<error ... source="detekt.RuleName"/>` (memory #155).
- Under D1=B the overall-0 `detektMain` gate **subsumes** the per-rule Set-C/LPL gates — LPL is only active in type-resolution mode, so a zero-findings `detektMain` run covers it. CI needs no report grepping.
- `:app:testDebugUnitTest` is the debug-variant unit suite (Kotest on the JUnit platform via `testOptions.unitTests.all.useJUnitPlatform()`).
- With default properties, the quarantined `RecipeScrapingTests` spec is skipped (§2), so the suite is deterministic offline.
- The "There were 33 compiler errors found during analysis" warning printed by the `:app` variant tasks is an environment constant (classpath incompleteness, memory #149): it is printed during analysis but does **not** affect the exit code — the exit code is findings-driven. See §4 hazard 2.

### No-red-commit ordering

`ci.yml` is **drafted** in task 5.1 (working-tree file only — explicitly NOT committed yet). The human commits the workflow only after task 3.1 (re-measure gate) and task 4.1 (quarantine) are green. Task 5.2 proves the exact yml command matrix locally, all green, so the "CI green" commit is guaranteed green: every command in the yml has already been run clean locally on the tree being committed.

## 2. Test quarantine (task 4.1 → bead `skillet-7ph.6`)

### API verified against Kotest 6.2.3 (binary-compat `api/*.api` dumps at tag v6.2.3)

- `io.kotest.core.annotation.Condition` — `fun evaluate(spec: KClass<*>): Boolean` (SAM-compatible).
- `io.kotest.core.annotation.DisabledIf(value: KClass<out Condition>)` — spec-class annotation; the spec is disabled when `evaluate` returns `true`.
- **`@IgnoredWhen` does not exist in 6.2.3** (only the hard `@Ignored` does). `@DisabledIf` + a custom `Condition` is the 6.x mechanism that implements the locked Q-A semantics exactly: default-off, property opt-in, no build-file filtering.
- Project-level `io.kotest.config.AbstractProjectConfig` (`enableIf`/`disableIf`/`ignore`) exists but is not needed — spec-level annotation is sufficient.

### Shape (target: `app/src/test/java/com/kronos/skilletapp/RecipeScrapingTests.kt`)

```kotlin
class ScrapeGate : Condition {
  // true => spec disabled. Quarantine is ON unless the run explicitly opts in.
  override fun evaluate(spec: KClass<*>): Boolean =
    System.getProperty("skillet.scrape") != "true"
}

@DisabledIf(ScrapeGate::class)
class RecipeScrapingTests : FunSpec({ /* body unchanged */ })
```

- Same-file, minimal diff: condition class + one annotation + two imports (`io.kotest.core.annotation.Condition`, `io.kotest.core.annotation.DisabledIf`, `kotlin.reflect.KClass` if not already imported). Spec body stays byte-identical.
- Opt-in: `skillet.scrape=true` as a **test-JVM system property** (e.g. `./gradlew :app:testDebugUnitTest -Dskillet.scrape=true` — the Smith must verify the property actually reaches the test JVM; if Gradle daemon→test-JVM propagation is unreliable, add a one-line `systemProperty` passthrough in `app/build.gradle.kts` `testOptions` — that is a build-file change ONLY if needed, and it stays a property gate, not a test filter).
- Verification: default `./gradlew :app:testDebugUnitTest` is green with the spec **skipped** (ignored, not failed); opt-in run shows the spec **executed** (network required — if unavailable locally, verify via targeted `--tests` run that the spec is discovered and *not* skipped).
- `ScrapeRecipeTests.kt` (offline, fixture-mocked) is **untouched** — keeps running in CI.
- Behavior contract: default run → spec skipped (suite deterministic offline); opt-in run → spec executes (network-dependent; its known 2-fail state is tolerable only in opt-in runs, never in CI).

## 3. Fix Band (tasks 1.1–3.1 → beads `skillet-7ph.1`–`.5`)

### Why measure-first

The finding set is in FLUX — the human has been fixing/refactoring since the last inventory. Stale inventory (memory #166, 2026-10-14) for orientation only:

| Module | File:line | Rule | Status at 2026-10-14 |
|--------|-----------|------|----------------------|
| `:utils` | `MiscUtils.kt:4` | UnusedImport | possibly already fixed — verify |
| `:utils` | `NumberUtils.kt:34` | HasPlatformType (on a property) | property may be gone — verify |
| `:measurement` | `Measurement.kt:113,116` | SpreadOperator ×2 | still present (re-verify) |
| `:measurement` | `MeasurementConverter.kt:3` | UnusedImport `MeasurementUnit.None.baseUnit` | file **moved** to `measurement/src/main/kotlin/com/kronos/measurement/model/` — re-check at new path |
| `:measurement` | `MeasurementConverter.kt:240` | UnusedPrivateFunction `reverseWith` | same — re-check |
| `:measurement` | `MeasurementUnit.kt:339` | NoNameShadowing `it` | re-check |
| `:app` | e.g. `AddEditRecipeViewModel.kt` | NoNameShadowing / UseCheckNotNull (analysis-API, flaky) | flaky — multi-run required |

None are Set-C rules; none of this is feature work.

### Measure-first protocol (task 1.1 → bead `skillet-7ph.1`)

1. Fresh reports: `./gradlew :utils:detektMain :measurement:detektMain :app:detektDebug :app:detektRelease --rerun-tasks` (force new mtimes — never trust stale report files; memory #150).
2. Parse each report (checkstyle format): rule, file, line, count.
3. **Repeat the whole matrix 3 times** — analysis-API rules (`NoNameShadowing`, `UseCheckNotNull`) are run-to-run flaky on identical code (memories #133/#141). The worklist is the **union** across the 3 runs; record per-site occurrence counts.
4. Also run plain (PSI) `detekt` on all three modules — must be 0 (deterministic baseline; any non-zero here is a distinct finding, report it).
5. **`:app` reachability verdict** (hazard 2): from the variant reports, record total findings + presence of the "33 compiler errors" warning. Zero is *proven* at re-measure (task 3.1), but the measure bead must judge whether residual `:app` findings look source-fixable. **If any `:app` finding appears to be a persistent analysis-API false positive not resolvable in source, STOP and escalate** (`bd human skillet-7ph.1`) — gating `:app` CI on an unreachable 0 would be silently wrong (flagged decision F1, §5).
6. Close the bead with the authoritative worklist in `bd close --reason` (updated table, same shape as above). Fix beads `.2`–`.4` execute **from that worklist**, not from this document's stale table.

### Fix discipline (tasks 2.1–2.3 → beads `skillet-7ph.2`–`.4`)

- Minimal, behavior-preserving style fixes; no signature changes; no restructuring beyond what a finding demands (dlm/ym4 band discipline; these are out-of-scope lint fixes, not feature work).
- Per-rule patterns: `UnusedImport` → remove the import line; `UnusedPrivateFunction` → remove the dead function (confirm no references first); `HasPlatformType` → explicit non-platform type; `SpreadOperator` → explicit construction/`.plus()` (behavior-preserving); `NoNameShadowing` → rename the shadowed variable; `UseCheckNotNull` → restructure to `checkNotNull`/`requireNotNull` per the rule's demand.
- Formatting: after edits, `./gradlew :<module>:ktfmtCheck` green (use `ktfmtFormat` on the module if edits disturb formatting — formatter, not manual reformatting).
- Per-bead verification: **3 consecutive** `:module:detektMain` runs exit 0 (flake: one clean run proves nothing — memories #133/#141).

### Re-measure gate (task 3.1 → bead `skillet-7ph.5`)

3 consecutive green passes of the full matrix:

1. `./gradlew :utils:detektMain :measurement:detektMain :app:detektMain` exit 0 (`:app` = both variants via delegation)
2. `./gradlew :utils:ktfmtCheck :measurement:ktfmtCheck :app:ktfmtCheck`
3. `./gradlew :utils:test :measurement:test :app:testDebugUnitTest` (default properties — quarantine on)
4. Plain `detekt` on all three modules stays 0 (PSI baseline preserved)

## 4. Hazards → design responses

| Hazard | Design response |
|--------|-----------------|
| Flaky analysis-API rules (`NoNameShadowing`, `UseCheckNotNull`) | 3-run measure (union worklist); 3-run verify per fix bead; 3-pass re-measure gate; **no single-run "clean" claims anywhere** in any bead's acceptance |
| `:app` "33 compiler errors" classpath caveat (memory #149) | Measure bead records warning presence + finding set + reachability verdict; **escalate to human if 0 is unreachable** (flag F1); CI gates on exit code only, which is findings-driven (warning does not flip it) |
| No red commits | yml drafted in 5.1 but committed by the human only after 3.1 + 4.1 green; 5.2 dry-runs the exact yml matrix locally first |
| Finding-set flux (concurrent human refactoring) | Worklist pinned by the measure-first bead at execution time; this document's table is explicitly stale/orientation-only |

## 5. Open / flagged decisions

- **F1 (only if triggered by the measure bead)**: `:app` full-green is unreachable or not reliably stable because of the variant-task classpath incompleteness → the human decides the CI treatment for `:app` (options to be offered at escalation: keep full-green with an accepted flake risk; fall back to PSI-mode `:app:detekt` + per-rule LPL gate on the variant reports; or a `continue-on-error` `:app:detektMain` lane). Not pre-decided — surfaced, per the planning constraint, rather than assumed away.

No other open decisions.

## 6. Non-goals

- No `detekt.yml` changes (five Set-C rules already active — D1=B needs no config change).
- No changes to `ScrapeRecipeTests.kt` or any offline test.
- No new domain code; no signature changes in the Fix Band.
- No emulator/instrumented test runs in CI (`connectedAndroidTest` stays local).
- No release/APK pipeline — CI is quality gates only.
