# Tasks: skillet-7ph

Workstream map (each task = one bead, fresh session each; Smith implements, Archivist flips this checkbox + closes the bead, human commits):

| Task | Bead |
|------|------|
| 1.1 | `skillet-7ph.1` |
| 2.1 / 2.2 / 2.3 | `skillet-7ph.2` / `.3` / `.4` |
| 3.1 | `skillet-7ph.5` |
| 4.1 | `skillet-7ph.6` |
| 5.1 / 5.2 | `skillet-7ph.7` / `.8` |

## 1. Fix Band — measure first

- [ ] **1.1** Run the fresh full type-resolution measurement (bead `skillet-7ph.1`):
  1. `./gradlew :utils:detektMain :measurement:detektMain :app:detektDebug :app:detektRelease --rerun-tasks`
  2. Parse the checkstyle reports (`utils/build/reports/detekt/main.xml`, `measurement/build/reports/detekt/main.xml`, `app/build/reports/detekt/{debug,release}.xml`): rule, file, line, count.
  3. Repeat steps 1–2 a total of **3 times**; worklist = union across runs, with per-site occurrence counts (analysis-API flake: `NoNameShadowing`/`UseCheckNotNull`).
  4. Plain PSI `detekt` on all three modules — must be 0; report any non-zero distinctly.
  5. **`:app` reachability verdict**: record the "33 compiler errors" warning presence and judge whether residual `:app` findings are source-fixable. If any look like persistent analysis-API false positives → `bd human skillet-7ph.1` and stop (flag F1 — do not proceed to fix/CI gating on an unreachable 0).
  6. **Acceptance**: bead closed with the authoritative worklist table (module / rule / file:line / occurrences-across-3-runs) in `bd close --reason`, replacing the stale table in `design.md` §3; `:app` verdict recorded; no source files modified.

## 2. Fix Band — fixes (worklist from task 1.1, not from `design.md`)

- [ ] **2.1** Fix every `:utils` finding on the 1.1 worklist (bead `skillet-7ph.2`): minimal, behavior-preserving edits in `utils/src/main/kotlin/**` (expected candidates: `MiscUtils.kt` UnusedImport, `NumberUtils.kt` HasPlatformType — verify against worklist). **Acceptance**: 3 consecutive `./gradlew :utils:detektMain` runs exit 0; `./gradlew :utils:ktfmtCheck` green; `./gradlew :utils:test` green; no signature changes.
- [ ] **2.2** Fix every `:measurement` finding on the 1.1 worklist (bead `skillet-7ph.3`): minimal, behavior-preserving edits in `measurement/src/main/kotlin/**` (expected candidates: `Measurement.kt` SpreadOperator ×2, `model/MeasurementConverter.kt` UnusedImport + UnusedPrivateFunction `reverseWith`, `MeasurementUnit.kt` NoNameShadowing — verify against worklist, file paths are post-move). **Acceptance**: 3 consecutive `./gradlew :measurement:detektMain` runs exit 0; `./gradlew :measurement:ktfmtCheck` green; `./gradlew :measurement:test` green; no signature changes.
- [ ] **2.3** Fix every `:app` finding on the 1.1 worklist (bead `skillet-7ph.4`): minimal, behavior-preserving edits in `app/src/main/{kotlin,java}/**` (expected candidates: flaky analysis-API sites, e.g. `AddEditRecipeViewModel.kt` NoNameShadowing/UseCheckNotNull — verify against worklist). **Acceptance**: 3 consecutive `./gradlew :app:detektMain` runs exit 0 (both variants); `./gradlew :app:ktfmtCheck` green; `./gradlew :app:testDebugUnitTest` green; `:app:compileDebugKotlin` green; no signature changes.

## 3. Fix Band — re-measure gate

- [ ] **3.1** Full re-measure, 3 consecutive green passes (bead `skillet-7ph.5`): (1) `./gradlew :utils:detektMain :measurement:detektMain :app:detektMain` exit 0; (2) `./gradlew :utils:ktfmtCheck :measurement:ktfmtCheck :app:ktfmtCheck` green; (3) `./gradlew :utils:test :measurement:test :app:testDebugUnitTest` green (default properties — quarantine on); (4) plain `detekt` on all three modules stays 0. **Acceptance**: all three passes identical-green (a pass that differs from its siblings is a flake signal — investigate before closing); no source files modified.

## 4. Test quarantine (D2 = Q-A)

- [ ] **4.1** Quarantine `RecipeScrapingTests` (bead `skillet-7ph.6`): in `app/src/test/java/com/kronos/skilletapp/RecipeScrapingTests.kt`, add a Kotest `Condition` class (`evaluate` returns `System.getProperty("skillet.scrape") != "true"`) + `@DisabledIf(...)` on the spec class — spec body byte-identical, same-file minimal diff. Opt-in = test-JVM system property `skillet.scrape=true` (add a `testOptions` property passthrough in `app/build.gradle.kts` ONLY if `-D` propagation to the test JVM proves unreliable — property gate, never a test filter). **Acceptance**: default `./gradlew :app:testDebugUnitTest` green with the spec skipped (ignored, not failed); opt-in run shows the spec executed (or, if no local network, a targeted `--tests` run proving the spec is discovered and not skipped); `ScrapeRecipeTests.kt` untouched; `:app:ktfmtCheck` green.

## 5. CI pipeline (D1 = B)

- [ ] **5.1** Draft `.github/workflows/ci.yml` (bead `skillet-7ph.7`): `on: push (main) + pull_request`; two jobs — `jvm` (no SDK: `./gradlew :utils:ktfmtCheck :measurement:ktfmtCheck :utils:detektMain :measurement:detektMain :utils:test :measurement:test`) and `app` (JDK 21 + `sdkmanager --install "platforms;android-37" "platform-tools"` + licenses: `./gradlew :app:ktfmtCheck :app:detektMain :app:testDebugUnitTest`); both jobs use `actions/checkout@v4`, `actions/setup-java@v4` (temurin 21), `actions/setup-gradle@v4`; gate = exit code only. **Acceptance**: YAML valid; job/command matrix exactly matches `design.md` §1; file is DRAFTED (working tree only — NOT committed; no-red-commit rule); no `detekt.yml` changes.
- [ ] **5.2** Final no-red-commit gate + handoff (bead `skillet-7ph.8`): run the yml's exact command matrix locally — `./gradlew :utils:ktfmtCheck :measurement:ktfmtCheck :utils:detektMain :measurement:detektMain :utils:test :measurement:test` and `./gradlew :app:ktfmtCheck :app:detektMain :app:testDebugUnitTest` — all green, plus plain `detekt` ×3 at 0. **Acceptance**: every command in `ci.yml` proven green on the tree to be committed; handoff note to the human: commit order = Fix Band fixes + quarantine + `ci.yml` together (single "CI green" state), then push to observe the first GitHub Actions run; **do not** close — Archivist closes on the human's commit + green Actions run evidence.
