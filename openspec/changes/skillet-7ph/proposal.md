# Proposal

## Why

Skillet has no CI. The quality invariants established by the `dlm`/`ym4` bands (zero-baseline style health, five Set-C complexity rules) are enforced manually only, and the two network-dependent tests in `RecipeScrapingTests` make the default unit suite flaky offline. The locked Gate-1 decisions (human, 2026-10-14) require: **D1 = B (fix-then-full)** — CI runs `detektMain` (full/type-resolution mode) on all three modules and requires **overall 0 findings**, which is only achievable after a Fix Band resolves the pre-existing out-of-scope findings — and **D2 = Q-A** — quarantine `RecipeScrapingTests` via a default-off Kotest property gate (no build-file test filtering).

## What Changes

- **CI pipeline** (new `.github/workflows/ci.yml`, no committed config): on `push` to `main` and on PRs, two jobs enforce ktfmt clean + `detektMain` full-green (overall 0 findings) + unit tests green — a JVM lane (`:utils`, `:measurement`) and an Android lane (`:app`).
- **Fix Band** (prerequisite, in scope per D1=B): resolves ALL pre-existing out-of-scope full-mode findings in `:utils`, `:measurement`, `:app` with minimal, behavior-preserving style fixes. The finding set is in flux (concurrent human refactoring), so a **measure-first** bead runs a fresh multi-run type-resolution measurement across all modules to pin the authoritative worklist before any fix.
- **Test quarantine** (D2=Q-A): Kotest 6.2 `Condition` + `@DisabledIf` gate on `RecipeScrapingTests` — skipped by default, opt-in via `skillet.scrape=true`. `ScrapeRecipeTests.kt` (offline, fixture-mocked) is untouched and keeps running in CI.

## Capabilities

### New Capabilities

- None — tooling/test-infrastructure change; no spec-level behavior change.

### Modified Capabilities

- None.

`skip_specs: true` is set in `.openspec.yaml` (precedent: archived `zero-baseline-style-health`).

## Impact

- **Added**: `.github/workflows/ci.yml`; quarantine `Condition` class + `@DisabledIf` on `RecipeScrapingTests` (same file).
- **Modified (Fix Band — worklist pinned by the measure-first bead, not this document)**: pre-existing findings in `:utils` (`MiscUtils.kt`, `NumberUtils.kt`), `:measurement` (`Measurement.kt`, `MeasurementConverter.kt` (now under `measurement/model/`), `MeasurementUnit.kt`), and `:app` (flaky analysis-API sites, e.g. `AddEditRecipeViewModel.kt`) — style-only edits, no signature changes.
- **Unchanged**: `detekt.yml` (the five Set-C rules are already active — no config change needed), `ScrapeRecipeTests.kt`, all domain behavior.
- **Hazards absorbed in design** (see `design.md` §4): (1) flaky analysis-API rules — every green claim is multi-run, never single-run; (2) the `:app` "33 compiler errors" classpath caveat — the measure-first bead must confirm `:app` genuinely and stably reaches 0 findings, or escalate as a flagged human decision rather than silently gating CI on it.
- **Ordering constraint (no red commits)**: the workflow file is drafted but committed by the human only after the Fix Band re-measure gate and the quarantine are green.
