# Design — zero-baseline-style-health (dlm Band 1)

Source-of-truth snapshot: `openspec/speculation.md` (Oracle Phase-1, 2026-09-25 photo) and
the three baseline files as of HEAD `753e9d3`. **All recorded counts in this document are
photographs; task 1 re-measures live and is authoritative.** Planning-time static
verification (2026-09-27, working tree clean) already found two deltas against the photo —
see D2/D8.

## D1 — Suppression mechanism after baseline deletion (the core decision)

**Problem.** Band 1 must end with all three baseline files deleted and `detekt` green in all
three modules, but the ~55 baseline-photo findings in the five Set-C refactor rules (LongMethod,
TooManyFunctions, CyclomaticComplexMethod, UnusedParameter, LongParameterList — the last has
never run in this project, so its live count is unknown) are out of scope for Band 1 (they land
in Band 2, design D7). Without a mechanism, deleting the baselines would turn those findings
into hard failures.

**Key discovery (2026-09-27, `detekt.yml` at HEAD; line numbers re-verified 2026-09-28).**
The shared root config already deactivates exactly those five Set-C rules:

```yaml
style:
  UnusedParameter:
    active: false
complexity:
  CyclomaticComplexMethod:
    active: false
  LongMethod:
    active: false
  LongParameterList:
    active: false
  TooManyFunctions:
    active: false
```

All three modules share this one config (`config.setFrom("$rootDir/detekt.yml")`,
`buildUponDefaultConfig = true`). Therefore:

- The ~34 baseline entries in those rules (18 LongMethod, 5 TooManyFunctions,
  3 CyclomaticComplexMethod, 8–9 UnusedParameter across the app baseline; LongParameterList
  has **zero** entries — it has never run in this project) are **orphan entries today** —
  the rules never report, so the entries suppress nothing.
- Deleting the three baseline files **cannot resurrect any finding**: the only live findings
  are the mechanical ones (WildcardImport, MagicNumber, MaxLineLength, ForbiddenComment,
  MatchingDeclarationName), and Band 1 fixes every one of them.
- **Chosen mechanism: none.** No residual baseline, no per-rule `excludes`, no
  `buildUponDefaultConfig` tweaks. The five Set-C rules simply stay `active: false` (their
  current, pre-existing state) until Band 2 re-enables them in B2.8 — and this is the state
  in which dlm's baseline-deletion commit (T8) is proven green. (Note: `style.UnusedPrivateClass`
  (lines 33–34), `style.ThrowsCount` (63–64), `exceptions.SwallowedException` (99) and
  `exceptions.TooGenericExceptionCaught` (101) are also `active: false` in the current
  `detekt.yml` — pre-existing, out of scope of this change; not part of Set C.)

**Band 2 handoff (locked 2026-09-28, human decision).** Band 2 = **Set C** — all five
rules above, bead `skillet-ym4`, tasks B2.1–B2.8 (design D7). The **first task** (B2.1) is
a **live measurement** of all five rules: scratch-enable them in the working tree (no
baselines exist at that point — dlm deleted them), run detekt in all three modules, record
the full per-rule finding set in a bead comment, restore the config. LongParameterList's
count is unknown until it runs (it has never run in this project). Fixes then land per file
(B2.2–B2.7) while the *committed* config still has the five rules off, so **every ym4
commit is green — no red window**; the enablement edit (`active: true` + per-rule Compose
tolerances) rides the **final commit** (B2.8) together with the full green sweep.
`skillet-7ph` is blocked on both `skillet-dlm` and `skillet-ym4`, so CI is never deployed
against a red or rules-off detekt. Between dlm close and Band 2 start, the five rules are
off — identical to today's state, i.e. no new hiding is introduced by this change; the
*new* guarantee is that **zero suppression lives in a baseline file** (human hard
constraint: "nothing hidden in a baseline at the end state" — satisfied from dlm's final
commit onward, not just from Band 2).

**Rejected alternatives.**
- *Residual baseline at dlm close, deleted at Band 2 close* — violates the letter of the
  human constraint during the interim and requires re-measuring an orphan set to curate.
- *Per-rule `excludes` in `detekt.yml`* — hides entire files/rule areas, coarser than the
  baseline it would replace, and leaves config debt in the final state.
- *`@Suppress` annotations* — in-code suppression; worse than baseline.
- *Fix all ~55 in Band 1* — rejected by the human (scope); the 18 LongMethod composable
  splits are genuine refactors with KoinPreview/test ripples (edge case 4 of the scout
  report).

## D2 — TODO migration (human option (b))

`bd todo add "<title>"` (verified present in the installed bd: shorthand for
`bd create -t task -p 2`) creates one issue per `TODO:` comment, then `bd update <id>
--description ...` records **file path, line, exact comment text**, and the origin
("migrated during skillet-dlm / zero-baseline-style-health"). `bd link <todo-id>
skillet-dlm --type discovered-from` links it. The comment is then deleted from source
(its line removed, not blanked — `ktfmtFormat` would reflow anyway).

**Live count delta (planning-time verification, 2026-09-27):** the app baseline holds 18
`ForbiddenComment` entries, but one of them (`RecipeScreen.kt: sort ingredients so that
quantity-based ingredients come first`) has **no matching comment in current source** — the
comment was removed by a later workstream. The other entry pointing at
`MeasurementUnit.kt` is live but lives in `:measurement`, not `:app`. Verified live set at
planning time: **17** (16 in `:app` src/main, 1 in `:measurement` src/main). The `/*TODO*/`
lambda markers in `RecipeListScreen.kt`/`RecipeScreen.kt`/`AddEditRecipeScreen.kt` are
*not* findings: `detekt.yml` sets `ForbiddenComment` values to `FIXME:`/`STOPSHIP:`/
`TODO:` (colon required), so bare `TODO` does not match — they are left alone.
**Task 1's live measurement is authoritative: migrate exactly the set detekt reports;
expect 17, accept more, and note any delta.**

## D3 — Task ordering and the formatter-last trap

Expanded imports and named constants change line counts/lengths; split strings change file
lengths. Therefore the final `ktfmtFormat` + full check sweep runs **after** all content
fixes and **before** the baseline-deletion commit, so the deletion commit is proven green on
the exact final file contents. Dependency chain (detailed in `tasks.md`):

```
T1 live re-measure ──► T2 ktfmtFormat sweep ──► T3 expand imports ──► T4 name constants
     ──► T5 split strings ──► T6 migrate TODOs ──► T7 final format+check sweep
     ──► T8 delete baselines+wiring (green sweep) ──► T9 bead/handoff closeout
```

No format/fix task is upstream of T7; T8 is the only step that touches build files and
deletes anything.

## D4 — Live re-measurement (task 1) and how to measure "live findings" with baselines in place

Bridging baselines suppress findings at report time, so a plain `detekt` run is green and
useless. Task 1 temporarily replaces each module's baseline with an **empty baseline** at the
same wired path (do not commit the swap):

```xml
<SmellBaseline>
  <ManuallySuppressedIssues/>
  <CurrentIssues/>
</SmellBaseline>
```

then runs `./gradlew :app:detekt :utils:detekt :measurement:detekt` and records the full
failure output (per-module, per-rule, per-file:signature), restores the original baselines,
and additionally runs `./gradlew :app:ktfmtCheck` (and the two module equivalents) to record
the live dirty-file set. Results are recorded in a comment on `skillet-dlm` and reconciled
against this file's photos. Any delta (files added/removed since 2026-09-25, entries fixed
by other workstreams, new findings from new code) is reported in the handoff; **task sizes
and file lists in tasks.md are derived from the live record, not from this document.**

## D5 — Merge-conflict posture

The entity-domain-split and room3 workstreams have already merged at HEAD `753e9d3`, so the
overlap risk is with any in-flight branches (and with the Band 2 bead itself, which edits the
same screens). Rules: (1) the ktfmt diff is canonical — if a hunk collides with a
functionality-branch change, resolve by re-running `ktfmtFormat`, never by hand-merging
formatting; (2) mechanical detekt fixes (imports/constants/strings) are line-local and
trivially re-apply on conflict; (3) the baseline-deletion commit touches only 3 build files
+ 3 deletions — conflict-free against any source branch.

## D6 — No-behavior-change guarantee

Imports, constants, comment deletions, and string splits (concatenation preserves value; the
6 long strings are data/preview strings in `ComposeUtils.kt` ×3 and `RecipeRepository.kt` ×3,
all of which are `String` literals in fake/preview data) cannot change runtime behavior.
Gate: `./gradlew test` green before and after, unchanged (Kotest unit suite, including the
907-line `MeasurementTests.kt`). `:app:lint` is not part of this change's gate (its state is
tracked by the g1 baseline audit, not dlm).

## D7 — Band 2 design: Set C (bead `skillet-ym4`, rescooped 2026-09-28)

**Scope.** All five Set-C rules re-enabled and their findings fixed — no baselines, no
`active: false` leftovers, no `@Suppress` (human hard constraint). Band 2 is a separate
execution stream (its own commit series, its own bead) but part of this change's
documented plan.

**Per-rule config (locked; Compose tolerances only).**
- `LongMethod` — defaults (60/100), no tolerance.
- `CyclomaticComplexMethod` — defaults (15/25), no tolerance.
- `UnusedParameter` — default, no tolerance.
- `TooManyFunctions` — `ignoreAnnotatedFunctions: ['Preview']` (required: TTF fires on
  files with many composables, including previews).
- `LongParameterList` — initial values `functionThreshold: 8` (raised from the default 6)
  plus `ignoreDefaultParameters: true`. **Initial values only — final values are set in
  B2.8 against B2.1's live set** (LPL has never run in this project, so its count is
  unknown until B2.1).

**Fix ordering (B2.1–B2.8) and the conflict-magnet decision.** In-flight beads mutate the
same files as the Set-C findings (`skillet-35e` AddEditRecipeScreen split,
`skillet-99f` unit-selection bottom sheet, `skillet-ocr` cook-mode timer, `skillet-y5y`
measurement workstream). Decision:
- **B2.1 — Live measurement (first, always).** Scratch-enable all five rules in the
  working tree (no baselines exist — dlm deleted them), run `detekt` in all three
  modules, record the full per-rule finding set in a `skillet-ym4` bead comment,
  reconcile against the 2026-09-17 baseline photo (especially the unknown LPL count),
  restore the config. All later tasks are *set-driven* from this measurement.
- **B2.2 — `AddEditRecipeScreen.kt` dead-parameter removal (early, mechanical).** Remove
  the 7 UnusedParameter findings from the `AddEditRecipeContent` signature (verified at
  753e9d3, line 219: `description`, `equipment`, `onDescriptionChanged`,
  `onEquipmentChanged`, `onMoveEquipment`, `onRemoveEquipment`, `onUserMessage`). Local,
  mechanical, no behavior change (the params are dead), and it shrinks the signature
  before LPL work. If `skillet-35e` has landed first, re-measure and drop whatever is
  already resolved (set-driven).
- **B2.3 — Other `:app` files, one fix pass per file, each re-measured before fixing**:
  `RecipeScreen.kt` (LM ×4 + file-level TTF), `AddEditRecipeViewModel` (TTF),
  `CookingScreen` (LM + UP index), `RecipeListScreen` (LM), `IngredientComponent` (LM ×2),
  `ComposeUtils:KoinPreview` (LM — edge case 4: if a split changes a preview composable's
  signature, update `KoinPreview`'s Koin module and call sites in the same commit),
  `RecipeRepository`/`initFakeRecipes` (LM), `UnitSelectionBottomSheet` (LM).
- **B2.4 — `SkilletNavGraph.kt` (LM) — careful review**: the fix must not change route or
  deep-link behavior (verify against deep-link tests + `./gradlew test`).
- **B2.5 — `Theme.kt` dynamicColor**: fix = **wire it up, don't delete**. Verified at
  753e9d3: `SkilletAppTheme` (line 39) takes `dynamicColor: Boolean = true` (line 42,
  unused) with the `dynamicColorScheme` branch commented out (lines 47–50). Uncomment the
  `Build.VERSION_CODES.S` branch (min SDK 30 < S (31), so the guard stays). A small
  feature, not a deletion.
- **B2.6 — `AddEditRecipeScreen.kt` LongMethod ×6 / CyclomaticComplexMethod ×2 / file
  TTF / LPL hit — last among `:app`.** Rationale: `skillet-35e` (in-flight, splitting the
  same file) and the other in-flight UI beads mutate this file; the structural
  extractions land against the file's *final* structure to avoid rework, while B2.2's
  mechanical param removal can land immediately. Fresh `git status` + re-measure before
  starting; if 35e has landed, the live set drives.
- **B2.7 — `:utils`/`:measurement` — last overall**: `Fraction` (TTF + CCM
  `unicodeFractionString`) and `Measurement` (TTF). Public API consumed by `:app` and the
  in-flight measurement workstream (`skillet-y5y`); coordinate before touching.
- **B2.8 — Final enablement.** Commit the `detekt.yml` edit: five rules `active: true`
  with the per-rule config above (LPL values finalized against B2.1's live set + B2.6's
  outcome). Same commit: the full green sweep — `ktfmtCheck`, `detekt` ×3, `./gradlew
  test` — proving the enablement lands green. Close `skillet-ym4`.

**Commit hygiene.** Every fix task commits its own change (message references
`skillet-ym4`) while the committed config still has the five rules off, verified green
via the scratch-enable trick (D4's pattern, applied to the rules). B2.8 is the only
commit that changes `detekt.yml`. No commit in the series is red.

**Stale-photo corrections (verified against the tree at 753e9d3, 2026-09-28).**
1. **`AddEditRecipeContent` is no longer a 7-parameter sub-composable** — it now takes
   **32 parameters** (verified at line 219; only `modifier` is defaulted). The 7 UP
   findings live inside that signature; removing them leaves **25** — still far above the
   proposed LPL threshold of 8, so LPL will flag `AddEditRecipeContent` regardless, and
   B2.6's fix is a real extraction (parameter object / sub-composable split), not
   threshold tuning. The planning premise "removing the 7 params likely resolves the
   worst LPL hit" does not hold against the current tree — flagged loudly to the
   human/Oracle.
2. **`TooManyFunctions:Fraction.kt` / `TooManyFunctions:Measurement.kt`** — the app
   baseline entries are stale module-split artifacts (the `utils`/`measurement` baselines
   are empty), so the live TTF findings for `Fraction`/`Measurement` surface in the
   `:utils`/`:measurement` runs; B2.1 records them.
3. **`RecipeScreen.kt` moved** to `ui/screen/recipe/RecipeScreen.kt` (entity-split
   workstream) — live file paths, not photo paths, drive the B2.x tasks.

**Acceptance.** All five Set-C rules `active: true` in `detekt.yml` (with the per-rule
config above); `detekt` green in all three modules with zero baselines and zero
`active: false` Set-C entries; `ktfmtCheck` and `./gradlew test` green.

## D8 — Deltas found between the 2026-09-25 photo and planning-time static state

1. `MatchingDeclarationName` is **pre-resolved**: `database/Converters.kt` was deleted by
   the merged entity-domain-split workstream (its task 6.2, which also moved the converter to
   `database/measurement/MeasurementConverters.kt`). No code fix needed; only the stale
   baseline entry exists, and it dies with the file deletion. The photo's "fix 1
   MatchingDeclarationName" becomes a verification no-op (task 1 confirms zero live
   findings).
2. One `ForbiddenComment` entry is stale (D2): 17 live TODO comments, not 18.
3. Wildcard drift: live `import x.*` lines in `src/main` total 46 (16 `:app` files listed in
   the live grep, of which `RecipeRepositoryImpl.kt`, `Equipment.kt`,
   `DefaultRecipeScraper.kt` carry wildcards absent from the 2026-09-25 baseline photo) —
   i.e. the live set has grown; task 1's run is the only authority.
4. Stale/orphan entry inventory in the app baseline (all die with file deletion, no surgical
   pruning required): Fraction ×8 (6 MagicNumber, 1 CyclomaticComplexMethod, 1
   TooManyFunctions), NumberUtils ×3 (2 MagicNumber, 1 WildcardImport), Measurement ×2
   (1 MagicNumber, 1 TooManyFunctions), MeasurementUnit ×1 (ForbiddenComment, live in
   `:measurement`), Converters ×1 (MatchingDeclarationName), plus the five Set-C rules' ~34 orphan entries
   (LongParameterList: zero — it has never run).
5. **`LargeClass:MeasurementTests.kt` / `WildcardImport:MeasurementTests.kt:3` — corrected
   (2026-09-28, S1, refuted live).** The photo claim that these entries were stale "in both
   baselines" was wrong on both counts: the entries exist **only in the measurement
   baseline** (4 entries total), and `:measurement` **does** scan test sources — its detekt
   block has no `source.setFrom`, and its `tasks.withType<Detekt> { exclude("**/test/**") }`
   block does not reliably exclude them (an empty-baseline `:measurement:detekt` run, S1,
   surfaced both findings live). The two test-source entries are **live suppressions, not
   stale** — deleting the baseline (8.1) without a scan-scope fix would turn them into new
   red findings at the 8.2 gate. Resolved by the source-set pin, design D10 (human-approved
   Option A).
6. **`java.*` wildcard not flagged (S4, observed detekt-2-alpha behavior).**
   `Equipment.kt:3` (`import java.util.*`) is the repo's only `java.*` wildcard and is the
   one wildcard line **not** flagged by `WildcardImport` (46 of 47 repo wildcard lines are
   flagged; the live run is the authority on all counts). Recorded as observed
   `2.0.0-alpha.6` behavior — **do-not-chase**: no investigation task, no config change;
   the live set in the 1.3 superseding record is authoritative.

## D9 — Band 1's one permitted `detekt.yml` edit (R1: `UnusedPrivateFunction` Preview tolerance)

**Trigger (verified at 753e9d3).** `detekt.yml` lines 27–29: `UnusedPrivateFunction` is
`active: true` with `allowedNames: ''` and **no** `ignoreAnnotated`. `app/src/main`
carries **10 private `@Preview` composables** (all 26 `@Preview` annotations in the module
are plain `@Preview`; zero multipreview annotations): `IngredientComponent.kt` ×2
(lines 286, 366), `ui/screen/recipe/RecipeScreen.kt` ×7 (lines 674, 701, 719, 738, 759,
774, 795), `AddEditRecipeScreen.kt` ×1 (line 1730). None of the three baselines contains
any UnusedPrivateFunction entry, so the first true (no-baseline) detekt run — task 1.1's
measurement — will report 10 new findings, and baselines are forbidden (human hard
constraint). The fix must land in Band 1.

**Resolution (locked).** Add `ignoreAnnotated: ['Preview']` to `UnusedPrivateFunction` —
detekt's official Compose guidance (https://detekt.dev/docs/introduction/compose).
`ignoreAnnotated` matches by annotation name; all 26 previews are plain `@Preview`, so
no multipreview annotation is needed. Pre-existing Compose tolerances already in
`detekt.yml` (verified, unchanged): FunctionNaming `functionPattern:
'[a-zA-Z][a-zA-Z0-9]*'` (line 72), MagicNumber `ignorePropertyDeclaration: true`
(line 54), TopLevelPropertyNaming `propertyPattern: '[A-Za-z][A-Za-z0-9_]*'` (line 82).

**Placement.** Applied uncommitted as **task 1.0** (step 0 of the section-1
measurement, before 1.1's empty-baseline run, so the live set the plan is built on
already excludes the 10 preview findings), and committed in **task 7.1 (commit 2)**
alongside the mechanical fixes — commit 1 stays format-only. Task 8.2's green sweep
re-proves it with the baseline deleted.

**Bounded diff (config + build files).** The complete Band-1 diff vs HEAD, across
`detekt.yml` and the three `build.gradle.kts`, is exactly: (1) `detekt.yml` — **one line**:
the R1 `ignoreAnnotated: ['Preview']` addition (already applied in the working tree,
2026-09-28, uncommitted; committed in task 7.1, commit 2); (2) `utils/build.gradle.kts` and
`measurement/build.gradle.kts` — **one added line each**: the
`source.setFrom("src/main/kotlin")` source-set pin (task 1.3, applied uncommitted; committed
in task 8.1, commit 3, design D10); (3) the three `baseline = file(...)` removals in task
8.1 (one per build file). No addition to `app/build.gradle.kts` (its baseline line removal
in 8.1 is its only change), no other build-file byte changes — the two
`tasks.withType<Detekt> { exclude(...) }` blocks stay byte-identical (kept as
defense-in-depth, D10). No other `detekt.yml` byte changes in Band 1.

## D10 — Deterministic test-source exclusion: pin `:utils`/`:measurement` detekt source sets (S1, Option A)

**Trigger (S1, verified live 2026-09-28).** Task 1.1's empty-baseline measurement (D4
mechanism) on `:measurement` surfaces `LargeClass:MeasurementTests.kt` and
`WildcardImport:MeasurementTests.kt:3` — both suppressed by the measurement baseline today
(D8.5 corrected). Cause: the `:utils` and `:measurement` detekt blocks have **no**
`source.setFrom`, so the plugin's default source set (which includes `src/test`) is scanned,
and the `tasks.withType<Detekt> { exclude("**/test/**") }` block proves unreliable
(`:measurement` clearly scans tests; `:app` is pinned at `app/build.gradle.kts:68` and is
deterministic). A baseline deletion (8.1) without a scan-scope fix would surface both
findings red at the 8.2 gate. The human reproduced S1 and approved **Option A**: make
test-source exclusion deterministic by pinning detekt sources to main, matching `:app`.

**Resolution (locked).** Add `source.setFrom("src/main/kotlin")` to the `:utils` detekt
block (`utils/build.gradle.kts`, between `allRules = false` and
`failOnSeverity = FailOnSeverity.Error`, becoming line 31) and the `:measurement` detekt
block (`measurement/build.gradle.kts`, same position, becoming line 47) — the mechanism
proven in `:app`. `:app` is untouched. **Fate of the exclude blocks: keep** both modules'
existing `tasks.withType<Detekt> { exclude(...) }` blocks unchanged — with the pin, test
sources are out of scan scope by construction, so the exclude lines become redundant
defense-in-depth at zero cost, and keeping them bounds the Band-1 build-file diff to
exactly two added lines (no deletions).

**Measurement delta (superseding record, task 1.3).** With the pin applied, the
empty-baseline three-module measurement is re-run and recorded as the **superseding**
finding set on `skillet-dlm`: the recorded 99-finding set is expected to drop to 97 (minus
`LargeClass:MeasurementTests.kt` and `WildcardImport:MeasurementTests.kt:3`);
`WildcardImport` total 46 → 45; `MagicNumber:Measurement.kt` and
`ForbiddenComment:MeasurementUnit.kt` remain (main source). **Verify against the live
record, not the arithmetic.** After the pin, the two test-source entries in the (still
wired) measurement baseline print as **orphan warnings** in real runs — warning-only
(`warningsAsErrors: false`, `FailOnSeverity.Error`), harmless until 8.1 deletes the file.
All fix-task count references (3.1–6.1) point at the 1.3 superseding record as
authoritative (D8 principle — no hard-coded count survives the re-measure).

**Placement.** Applied uncommitted in task 1.3 (its bead is wired between 2.1/commit 1 and
3.1, so commit 1 stays format-only), committed in **task 8.1 (commit 3)** alongside the
baseline unwiring — the only commit that touches build files. Task 8.2's green sweep
re-proves it with the baselines deleted: test sources are out of scan scope **by design**
(explicit source-set pin, not a baseline), and the zero-suppression gate stands unchanged.

## D11 — LongParameterList gate topology (B2.1 mechanism finding, addendum 2026-10-03)

**Mechanism finding (B2.1, `skillet-ym4.1`, closed; corroborated against on-disk reports).**
LongParameterList is the **only one of the five Set-C rules** annotated
`@RequiresAnalysisApi` in detekt 2.0.0-alpha.6 — LongMethod, TooManyFunctions,
CyclomaticComplexMethod and UnusedParameter are pure PSI. detekt-core
(`RuleDescriptor.kt`) force-disables `RequiresAnalysisApi` rules under
`AnalysisMode.light`, which is how the plain per-module `detekt` tasks run
(no classpath). B2.1's two-pass measurement is the evidence: Pass 1 (plain
`:app:detekt :utils:detekt :measurement:detekt`, light mode) reported **0 LPL
findings — a non-execution artifact, not an empty finding set** (log line:
"requires type resolution but it was run without it"); Pass 2 (type-resolution
`:app:detektMain :utils:detektMain :measurement:detektMain`, full mode) reported
**26 LPL findings, all `:app`** (largest: `AddEditRecipeContent`, 32 params), 0 in
`:utils`/`:measurement`. The four PSI rules re-ran in full mode with counts
identical to Pass 1, confirming mode-independence.

**Gate split (supersedes any reading of the Band 2 gate as "plain `detekt`
0/0/0 across all five rules"; D7's scope, fix ordering and per-rule settings are
unchanged — this decides only the verification surface).**
- The plain tasks (`:app:detekt :utils:detekt :measurement:detekt`) enforce the
  **four PSI rules** (LongMethod, TooManyFunctions, CyclomaticComplexMethod,
  UnusedParameter) under the existing zero-baseline **0/0/0 invariant**.
- **LongParameterList is enforced ONLY on the type-resolution surface**: per-rule
  LPL finding count = 0 in `:app:detektMain`'s full-mode reports
  (`app/build/reports/detekt/debug.xml` **and** `release.xml` — identical content,
  no variant-specific sources) and in `:utils:detektMain` /
  `:measurement:detektMain` (`main.xml` each).
- **Overall `detektMain` green is explicitly NOT a gate**: full-mode runs carry
  pre-existing out-of-scope findings (B2.1 side observation — `:app`
  UnusedPrivateProperty x6, NoNameShadowing x9, UnusedVariable x3, UseOrEmpty x5,
  InjectDispatcher + 2x UnusedImport in `SkilletApp.kt`; `:utils` UnusedImport in
  `MiscUtils.kt`; HasPlatformType/SpreadOperator/UseCheckNotNull across modules).
  The LPL gate is per-rule against that pre-existing noise, not whole-surface green.

**Effect on B2.8's enablement edit.** Flipping LongParameterList `active: true` in
`detekt.yml` (with `ignoreDefaultParameters: true` and the B2.8-tuned
`functionThreshold`) remains **required and correct** — it is what enables LPL on the
type-resolution surface — even though the flip is **inert on the plain tasks** (they
cannot see or enforce LPL at any threshold). The threshold/tolerance VALUES are still
decided at B2.8 runtime against the B2.1 record (initial values per D7:
`functionThreshold: 8` + `ignoreDefaultParameters: true`); this addendum moves only
the gate surface, not the values and not the five-rule scope.

## Risks

- **K1 — New findings in task 1.** The room3/entity-split merges changed files since the
  photo; the live run may show more findings than the photos (e.g. more MagicNumbers in
  reworked files). Task 1's reconciliation step exists for exactly this; the fix tasks are
  *set-driven* (fix what the live run reports) so extra findings don't break the plan, only
  extend a task.
- **K2 — dev.detekt alpha API drift.** The installed detekt is `dev.detekt:detekt-gradle-plugin
  2.0.0-alpha.6` (a 2.0-line alpha). The empty-baseline swap in D4 assumes the classic
  `SmellBaseline` XML format (confirmed by the three existing baseline files using it) and
  that the plugin tolerates the wired baseline file existing with an empty issue set (it
  does — the files are plain XML the plugin parses). If the alpha plugin rejects an empty
  baseline, fallback: temporarily comment the `baseline = file(...)` line per module for the
  measurement run only (restore before any commit).
- **K3 — Baseline "orphan" noise.** Running detekt with stale baselines prints orphan-entry
  warnings; they never fail the build (only *unsuppressed findings* fail). T8's green sweep
  is the proof, not the warning output.
- **K4 — `ktfmtCheck` on `:utils`/`:measurement`.** The photo says the 34 dirty files are all
  `:app`; if task 1 finds `:utils`/`:measurement` dirty at width 100, the format sweep (T2)
  extends to them — no scope change, same commit.
