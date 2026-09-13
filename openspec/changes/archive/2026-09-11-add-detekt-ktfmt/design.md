# Design: Add Detekt and ktfmt

## Context

The Skillet app is a single-module Android project (`app/`) using Gradle 9.3.1, Kotlin 2.4 (language version 2.4), and AGP 9.1.1. It has ~48 Kotlin source files in `app/src/main` with no static analysis or automated formatting. The project uses `kotlin.code.style=official` in `gradle.properties`. ANTLR generates Java files into `build/generated/`. There is no CI system.

## Goals / Non-Goals

**Goals:**
- Add Detekt v2.0.0-alpha.6 with Kotlin 2.4 compatibility
- Add ktfmt v0.27.0 with Google Kotlin style formatting
- Start with minimal Detekt rules to avoid build breakage
- Run both as optional Gradle tasks (not hooked into `check`/`build`)
- Run ktfmt as a one-time pass and commit the result
- Exclude generated code from both tools

**Non-Goals:**
- Hooking Detekt/ktfmt into CI (no CI exists; this is infrastructure only)
- Hooking Detekt/ktfmt into `check` or `build` lifecycle
- Fixing existing code style violations (beyond the one-time ktfmt pass)
- Adding new source files or changing application logic
- Configuring Detekt to fail the build — violations are informational initially

## Decisions

### D1: Detekt v2.0.0-alpha.6 (not v1.x)

**Decision:** Use Detekt `2.0.0-alpha.6` via the **`dev.detekt`** plugin ID.

**Rationale:** Kotlin 2.4 exceeds Detekt v1.x compiler compatibility (max Kotlin 2.1.x). Detekt v2.x uses `kotlin-compiler-embeddable` for native Kotlin 2.x support. There is no viable v1.x path.

**Important v2 migration notes:**
- **Plugin ID changed:** `dev.detekt` (not `io.gitlab.arturbosch.detekt`)
- **Version:** `2.0.0-alpha.6` (not `2.0.0`)
- **YAML config structure changed** from v1 — see D3 for details

**Alternatives considered:**
- Detekt v1.23.x — rejected: incompatible with Kotlin 2.4
- ktlint — rejected: Detekt is more widely adopted in Android projects, provides richer rule sets

### D2: ktfmt standalone plugin (not Detekt's built-in formatter)

**Decision:** Use `com.ncorti.ktfmt.gradle` v0.27.0 as a standalone plugin.

**Rationale:** Provides independent `ktfmtCheckMain`/`ktfmtFormatMain` tasks that can be gated in CI without Detekt overhead. Full ktfmt API access for future tuning. Detekt's `detektFormat` task wraps ktfmt but offers less configurability.

**v0.27.0 API:** Uses `googleStyle()`, `removeUnusedImports`, `maxWidth`, and `trailingCommaManagementStrategy` (no longer uses `kotlinStyle = Style.GoogleStyle()` or `googleMaxLineLength`).

**Alternatives considered:**
- `detektFormat` — acceptable but ties formatting to Detekt lifecycle; less flexible for CI gating

### D3: Minimal initial Detekt rule set (v2 YAML format)

**Decision:** Start with `buildUponDefaultConfig = false` and `allRules = false`, enabling only essential rules via explicit YAML entries.

**Critical v2 rule renames from v1:**
| v1 name | v2 name |
|---------|---------|
| `UnusedPrivateMember` | `UnusedPrivateFunction` + `UnusedPrivateProperty` (split) |
| `UnusedImports` | `UnusedImport` |
| `FunctionMaxLength` | `FunctionNameMaxLength` |
| `FunctionMinLength` | `FunctionNameMinLength` |
| `documentation` (rule set) | `comments` (rule set) |
| `bugs` (rule set) | `potential-bugs` (rule set) |

**YAML format changes from v1:**
- `active` values are **booleans** (`true`/`false`), not strings (`"true"`/`"false"`)
- No `android: true` — the Android profile is handled by the Gradle plugin integration
- No `autoCorrect` or `failFast` — these are removed in v2
- Config structure: `config.validation`, `excludes`, `processors`, `console-reports`, then rule sets

**Gradle config block handles:** `config.setFrom()`, `buildUponDefaultConfig`, `allRules`, `source.setFrom()`, `exclude()`, `failOnSeverity` — these are **NOT** in the YAML file.

**`failOnSeverity`** is a Gradle plugin setting: `failOnSeverity = FailOnSeverity.Error`

**Minimal rule set (explicitly enabled):**
- **Naming:** `FunctionNaming`, `ClassNaming`, `ObjectPropertyName`, `VariableNaming`
- **Potential bugs:** `UnusedPrivateFunction`, `UnusedPrivateProperty`, `DoubleMutabilityForCollection`, `EqualsAlwaysReturnsTrueOrFalse`
- **Comments:** `ForbiddenComment` (flags TODO/FIXME/STOPSHIP)
- **Exceptions:** `ExceptionRaisedInUnexpectedLocation`
- **Style:** `UnusedImport`

**Disabled (high false-positive risk):**
- `FunctionNameMaxLength` — Compose composables are naturally long
- `TooManyFunctions` — ViewModels with multiple states
- `DataClassNaming` — Not applicable to all data classes
- `Serializers` rules — May conflict with kotlinx.serialization
- `Duplicates` — ANTLR-generated code can cause false positives

**Rationale:** The project has ~48 Kotlin files with zero prior linting. With `buildUponDefaultConfig = false`, only rules explicitly listed in the YAML will run. Start conservative, enable rules incrementally after the baseline is established.

### D4: Detekt runs on main source set only (not tests initially)

**Decision:** Configure Detekt to analyze only `app/src/main/**.kt` and `app/src/main/**.java`. Exclude `src/test/` and `src/androidTest/` from the initial run.

**Rationale:** Test code has different style expectations (longer test names, setup methods, assertions). Test violations can be addressed in a second pass once the main code baseline is established.

**Exclusions go in `tasks.withType<Detekt>()` block (v2 location):**
```kotlin
tasks.withType<Detekt>().configureEach {
    exclude("**/generated/**")
    exclude("**/test/**")
    exclude("**/androidTest/**")
}
```
In detekt v2, `exclude()` moved from the `detekt {}` config block into the task configuration block.

### D5: ktfmt Google style, 100-char line width

**Decision:** `ktfmt { googleStyle(); removeUnusedImports = true; maxWidth = 100; trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE }`

**Rationale:** Google style is ktfmt's default and matches the project's existing 100-char convention. The `kotlin.code.style=official` property in `gradle.properties` is a Kotlin compiler setting (affects compiler diagnostics), not a formatter setting — ktfmt's style is independent.

**v0.27.0 API notes:**
- `googleStyle()` replaces `kotlinStyle = Style.GoogleStyle()`
- `removeUnusedImports = true` replaces `targetCharset` (ktfmt now handles imports)
- `maxWidth = 100` replaces `googleMaxLineLength`
- `trailingCommaManagementStrategy = TrailingCommaManagementStrategy.COMPLETE` for full trailing comma handling

**What ktfmt will change in a one-time pass:**
- String quotes (ktfmt prefers single quotes where possible)
- Import ordering (ktfmt uses alphabetical, case-insensitive)
- Spacing around operators and dots
- Line wrapping at ~100 chars
- Trailing commas (complete management)

### D6: ANTLR-generated code excluded

**Decision:** Both Detekt and ktfmt exclude `**/generated/**` and `build/` directories.

**Rationale:** ANTLR generates Java files in `build/generated/`. These are regenerated on every build and should never be linted or formatted.

### D7: Optional tasks only (not lifecycle hooks)

**Decision:** Detekt and ktfmt tasks are available but NOT hooked into `check` or `build`.

**Rationale:** Running both on every build adds ~5-15 seconds. Developers run them manually (`./gradlew detekt`, `./gradlew ktfmtCheckMain`) or in CI. This avoids slowing down daily development while still providing the tools.

### D8: Plugin registration pattern

**Decision:**
- Root `build.gradle.kts`: `alias(libs.plugins.detekt) apply false` — register without applying
- `app/build.gradle.kts`: `alias(libs.plugins.detekt)` and `alias(libs.plugins.ktfmt)` — apply to app module
- Versions in `gradle/libs.versions.toml` under `[versions]` and `[plugins]`
- Plugin IDs in TOML: `detekt = { id = "dev.detekt", version.ref = "detekt" }`

**Rationale:** Standard Gradle convention for multi-project setups. The root registers plugins with `apply false` so subprojects can apply them independently.

### D9: No incorrect imports in build.gradle.kts

**Decision:** Do not import `jdk.jfr.internal.JVM.exclude` or any other JDK internals. The `exclude()` method on `Detekt` tasks is a standard Gradle method — no import needed. Only import plugin-specific types: `dev.detekt.gradle.Detekt`, `dev.detekt.gradle.extensions.FailOnSeverity`, `com.ncorti.ktfmt.gradle.TrailingCommaManagementStrategy`.

**Rationale:** JDK internal imports are not part of the public API and may break across Kotlin/Gradle versions.

## Risks / Trade-offs

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| ktfmt reformatting causes a massive diff | Medium | Run `ktfmtFormatMain` once, commit as a single formatting-only change. Review the diff before committing. |
| Detekt finds dozens of violations on first run | High | Start with `buildUponDefaultConfig = false` + minimal rule set. Generate a baseline. Don't fail the build. |
| Detekt v2.0.0-alpha.6 has instability bugs | Low | Pin to this specific version. If issues arise, bump to the latest v2.x patch. |
| Gradle 9.3.1 incompatibility with plugin versions | Low | Both detekt-gradle-plugin 2.0.0-alpha.6 and ktfmt-gradle 0.27.0 target recent Gradle versions. Verify with a test build. |
| `.editorconfig` conflicts with IDE settings | Low | Standard settings (UTF-8, 4-space indent, trim trailing whitespace) are widely compatible. Android Studio reads `.editorconfig` by default. |
| YAML config structure mismatch with v2 | Low | Follow the exact structure from the detekt v2 default config; `active` is boolean, rule sets renamed. |

## Migration Plan

1. **Add plugin versions** to `gradle/libs.versions.toml` (detekt 2.0.0-alpha.6, ktfmt 0.27.0)
2. **Register detekt plugin** in root `build.gradle.kts`
3. **Apply plugins** in `app/build.gradle.kts` with configuration blocks (v2: exclusions in `tasks.withType<Detekt>()`)
4. **Create `detekt.yml`** with minimal v2 rule set (boolean `active`, renamed rules)
5. **Create `.editorconfig`** with standard settings
6. **Verify Gradle sync** — `./gradlew tasks --all | grep -E 'detekt|ktfmt'`
7. **Run ktfmt one-time pass** — `./gradlew ktfmtFormatMain`
8. **Review and commit** ktfmt formatting changes
9. **Run Detekt** — `./gradlew detekt` — capture output for baseline
10. **Decide on rule enablement** — review violations, suppress or fix as needed

## Open Questions

_None._ All technical decisions are resolved. Rule enablement after the first Detekt run is a post-implementation decision, not a planning blocker.
