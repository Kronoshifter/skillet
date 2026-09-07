# Proposal: Add Detekt and ktfmt

## Why

The Skillet app has no static analysis or automated code formatting tooling. With ~48 Kotlin source files and growing, the team lacks enforcement of code quality standards, consistent formatting, and early detection of potential bugs. Manual code review is the only quality gate currently in place, which doesn't scale well and is inconsistent across reviewers.

## What Changes

- Add **Detekt v2.0.0-alpha.6** (static analysis for Kotlin) with a minimal initial rule set to avoid breaking the build on first run
- Add **ktfmt v0.27.0** (Kotlin formatter based on Google's style) with check and format tasks
- Create `detekt.yml` configuration file with detekt v2 YAML format, generated-code exclusions, and a conservative rule set
- Create `.editorconfig` for IDE-level consistency (UTF-8, 4-space indent, final newline)
- Run ktfmt as a one-time formatting pass and commit the result
- Hook both as optional Gradle tasks — NOT into `check` or `build` lifecycle

## Capabilities

### New Capabilities

- **code-quality-tooling**: Automated static analysis via Detekt with configurable rules, Android-aware profiles, and generated-code exclusions
- **code-formatting**: Automated Kotlin code formatting via ktfmt with Google style, 100-char line width, and CI-checkable task

### Modified Capabilities

_None_ — this is pure tooling infrastructure. No behavioral requirements change.

## Impact

**Files modified:**
- `build.gradle.kts` (root) — register detekt plugin with `apply false`
- `app/build.gradle.kts` — apply detekt and ktfmt plugins, configure both
- `gradle/libs.versions.toml` — add detekt and ktfmt version and plugin entries
- `gradle.properties` — optional JVM args tuning

**Files created:**
- `detekt.yml` — Detekt v2 configuration (YAML format)
- `.editorconfig` — Editor configuration

**No source code changes.** ktfmt will reformat existing Kotlin files (one-time pass) as a consequence of running the formatter task.

**Dependencies added:**
- `dev.detekt:detekt-gradle-plugin:2.0.0-alpha.6` (plugin ID: `dev.detekt`)
- `com.ncorti.ktfmt.gradle:com.ncorti.ktfmt.gradle.plugin:0.27.0`
