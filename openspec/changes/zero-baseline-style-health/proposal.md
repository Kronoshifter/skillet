# Proposal

## Why

`./gradlew ktfmtCheck` is red in `:app` (34 unformatted files at the human-approved width-140
set recorded 2026-09-25) and `detekt` in all three modules is green **only because** every
finding is suppressed in one of three `SmellBaseline` files. Human policy (2026-09-27) is
unambiguous: baselines are not a final state — findings must actually be fixed, and nothing may
be hidden in a baseline at the end of this work. This blocks bead `skillet-dlm`, which in turn
blocks `skillet-7ph` (the CI pipeline) and unblocks the entity-domain-split merge
(`skillet-rm-m5r`), so this is the critical path for the whole project.

This change is **Band 1** of the dlm split (human-confirmed): make `ktfmtCheck` and `detekt`
green in all three modules (`:app`, `:utils`, `:measurement`) by fixing every *mechanical*
finding and *migrating* every `TODO:` comment to an issue, then delete all three baseline files
and their build-file wiring. The *refactor* findings — scoped as **Set C**, the five rules
`detekt.yml` has switched off (LongMethod, TooManyFunctions, CyclomaticComplexMethod,
UnusedParameter, LongParameterList; the last has never run, so its live count is unknown) —
are out of scope for the Band-1 sweep but part of this change's documented plan: bead
`skillet-ym4`, tasks B2.1–B2.8 below, design D7.

## What Changes

- **ktfmt**: run `:app:ktfmtFormat` over the live width-140 dirty set (34 files recorded
  2026-09-25, all under `app/src/main`; re-measured in task 1 — the recorded set is a photo,
  not a contract). Canonical width is 140 for `:app` and 100 for `:utils`/`:measurement`;
  **no width or formatter config changes** in this change.
- **detekt mechanical fixes only** (every enabled-rule finding that can be fixed without a
  refactor): expand all `WildcardImport`s, name all `MagicNumber` magic values as constants,
  split the 6 `MaxLineLength` string literals, and drop stale baseline entries. No
  signature, control-flow, or behavior changes. Plus exactly one Band-1 `detekt.yml` edit
  task (1.0), committed in 7.1: the R1 tolerance (`UnusedPrivateFunction`
  `ignoreAnnotated: ['Preview']` — 10 private `@Preview` composables would otherwise be new
  findings in the first true run; design D9).
- **Deterministic test-source exclusion (S1, design D10)**: pin the `:utils`/`:measurement`
  detekt source sets to `src/main/kotlin` (`source.setFrom("src/main/kotlin")`, the proven
  `:app` mechanism) — the empty-baseline `:measurement` run surfaced
  `LargeClass:MeasurementTests.kt` + `WildcardImport:MeasurementTests.kt:3` as
  baseline-suppressed live findings, so the baseline deletion needs the pin to land green
  with zero suppression; test sources are then out of scan scope by design, not by
  baseline. Task 1.3; uncommitted at measurement time, committed with the baseline
  unwiring (8.1).
- **`TODO:` comment migration (human option (b))**: every live `ForbiddenComment` finding in
  `src/main` (18 recorded; 17 verified live at planning time — see design D2) is migrated to a
  `bd` task issue carrying the file location and exact TODO text, then the comment is deleted
  from source.
- **Baseline elimination**: delete `detekt-baseline.xml`, `utils/detekt-baseline.xml`,
  `measurement/detekt-baseline.xml` and remove the `baseline = file(...)` line from each of
  the three `build.gradle.kts`. This is the last step, only after a full green sweep.
- **Bead graph**: Band 2 bead `skillet-ym4` (feature, p2, `discovered-from: skillet-dlm` —
  Beads allows one edge type per pair, so it carries no `blocks` edge; the dlm-before-ym4
  ordering is guarded in the bead's notes) carrying the Set C findings with per-file fix
  tasks B2.1–B2.8; `skillet-7ph` stays blocked by **both** `skillet-dlm` and `skillet-ym4`.
  (Bead-graph changes are executed by the Architect, per project memory #5.)
- **No behavior change**: `./gradlew test` must pass unchanged. One commit per concern
  (format / mechanical detekt / baseline deletion), deletion last.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None — pure tooling/style change; application behavior is unchanged, so no spec-level
  requirements are introduced or modified. (`skip_specs: true` is set in `.openspec.yaml`.)

## Impact

- **Code touched (all format/mechanical)**: ~34 `:app` formatted files + ~22 files receiving
  import/constant/string fixes (overlap expected) + 1 `:utils` file + 1 `:measurement` file.
  Merge-conflict overlap with the (now-merged) entity-domain-split and room3 workstreams is
  expected in `database/`, `data/`, and screen files; the formatter is the canonical resolver
  (re-run `ktfmtFormat`, never hand-resolve format hunks) — see design D5.
- **Build files**: `app/build.gradle.kts`, `utils/build.gradle.kts`,
  `measurement/build.gradle.kts` (baseline wiring removed; plus the `:utils`/`:measurement`
  source-set pins from task 1.3 — design D10); `detekt.yml` receives exactly two
  documented edits across this change — the single Band-1 R1 tolerance
  (`UnusedPrivateFunction.ignoreAnnotated: ['Preview']`, design D9, committed in task 7.1)
  and the Band-2 Set C enablement (B2.8, design D7). Its five `active: false` Set-C rules
  stay off until B2.8 (design D1).
- **Deleted files**: the three baseline XMLs.
- **Band 2 impact (B2.x, bead `skillet-ym4`)**: `:app` screen/component/VM files
  (`AddEditRecipeScreen` split into two passes — dead-param removal early (7 of the
  **32-parameter** `AddEditRecipeContent` signature, verified 2026-09-28 — the baseline
  photo's 7-parameter picture is stale, design D7 correction #1, so the LPL fix is a real
  extraction, not threshold tuning), extractions last, design D7), `SkilletNavGraph.kt`,
  `Theme.kt` (dynamicColor wiring), `Fraction.kt` /
  `Measurement.kt` in `:utils`/`:measurement` (public API — last, coordinated with the
  measurement workstream), and the `detekt.yml` enablement commit.
- **Bead graph**: `skillet-ym4` rescooped to Set C with per-file fix tasks
  (`discovered-from: skillet-dlm`); `skillet-7ph` blocked by both `skillet-dlm` and
  `skillet-ym4`; `skillet-dlm` description/acceptance rewritten to this finalized scope;
  +17–18 `bd todo` issues (`discovered-from: skillet-dlm`).
- **Not affected**: `:utils`/`:measurement` ktfmt config, Room schema, parser grammar,
  generated ANTLR code, test sources (test-source TODOs are out of scope — detekt scans
  `src/main` only).
