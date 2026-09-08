## 1. Run ktfmt format pass (wildcard import cleanup)

- [ ] 1.1 Run `./gradlew ktfmtFormatMain` to format all main source Kotlin files (wildcard import cleanup)
- [ ] 1.2 Run `git diff --stat` to review the scope of formatting changes (expected: import ordering changes, unused import removals in UI files)
- [ ] 1.3 Stage and commit the formatting changes with message: `style: clean wildcard imports with ktfmt (pre-detekt baseline)`
- [ ] 1.4 Verify the commit includes only formatting changes by reviewing the diff (no logic changes expected)

## 2. Rewrite detekt.yml with hybrid rule configuration

- [ ] 2.1 Replace the current `detekt.yml` with the hybrid configuration:
  - Keep `config.validation = true`, `warningsAsErrors = false`, `checkExhaustiveness = false`
  - Keep processor exclusions (same as current)
  - Keep `console-reports` exclusions (same as current)
- [ ] 2.2 Enable Phase 1 rules (safe, no code changes required):
  - `WildcardImport` with `active: true`
  - `ImportOrdering` with `active: true`
  - `UnusedPrivateFunction` with `active: true`
  - `UnusedPrivateProperty` with `active: true`
  - `ForbiddenComment` with `active: true` (same as current)
- [ ] 2.3 Enable Phase 2 rules with Compose-friendly overrides:
  - `FunctionNaming` with `active: true`, `functionPattern: '[a-zA-Z][a-zA-Z0-9]*'`, `excludeClassPattern: '$^'`
  - `ClassNaming` with `active: true`
  - `ObjectPropertyNaming` with `active: true`
  - `VariableNaming` with `active: true`
  - `MagicNumber` with `active: true`, `ignoreNumbers: ["-1", "0", "1", "2"]`, `ignoreExpressions: true`
  - `TopLevelPropertyNaming` with `active: true`, `pattern: '[a-zA-Z][a-zA-Z0-9_]*'`
  - `DoubleMutabilityForCollection` with `active: true` (keep from current)
  - `EqualsAlwaysReturnsTrueOrFalse` with `active: true` (keep from current)
  - `ExceptionRaisedInUnexpectedLocation` with `active: true` (keep from current)
- [ ] 2.4 Explicitly disable noisy rules:
  - `LongMethod` with `active: false`
  - `LongParameterList` with `active: false`
  - `TooManyFunctions` with `active: false`
  - `ComplexMethod` with `active: false` (deferred to Phase 3)
  - `CyclomaticComplexMethod` with `active: false` (deferred to Phase 3)
  - `SwallowedException` with `active: false` (deferred to Phase 3)
  - `TooGenericExceptionCaught` with `active: false` (deferred to Phase 3)
  - `UnusedPrivateClass` with `active: false` (deferred)
  - `UnusedParameter` with `active: false` (deferred)
  - `ThrowsCount` with `active: false` (deferred)
- [ ] 2.5 Verify the YAML is valid by running `./gradlew detekt --warning-mode all` and confirming no YAML parse errors

## 3. Update app/build.gradle.kts for expanded ruleset

- [ ] 3.1 Change `buildUponDefaultConfig` from `false` to `true` in the `detekt {}` block
- [ ] 3.2 Add `allRules = false` to the `detekt {}` block (prevents coroutines/performance noise)
- [ ] 3.3 Add `baseline = file("$rootDir/detekt-baseline.xml")` to the `detekt {}` block
- [ ] 3.4 Verify Gradle sync succeeds by running `./gradlew help` with no errors

## 4. Generate detekt-baseline.xml

- [ ] 4.1 Run `./gradlew detekt` to capture all violations with the expanded ruleset
- [ ] 4.2 Generate the baseline file using `./gradlew detekt --baseline detekt-baseline.xml` (project root; or `detekt baseline` command)
- [ ] 4.3 Review the generated `detekt-baseline.xml` to ensure it only contains expected violations (wildcard imports, magic numbers, unused private members, naming issues)
- [ ] 4.4 Verify the baseline file is valid XML and contains `<Findings>` root element with rule-specific entries

## 5. Commit baseline and verify clean detekt run

- [ ] 5.1 Add `detekt-baseline.xml` to git and commit with message: `chore: add detekt baseline for expanded ruleset`
- [ ] 5.2 Run `./gradlew detekt` and verify zero violations (baseline suppresses known issues)
- [ ] 5.3 Run `./gradlew ktfmtCheckMain` and verify zero formatting violations (ktfmt pass succeeded)
- [ ] 5.4 Run `./gradlew :app:assembleDebug` and confirm the build still compiles successfully
- [ ] 5.5 Run `./gradlew :app:testDebugUnitTest` and confirm existing unit tests still pass

## 6. Document deferred rules for future work

- [ ] 6.1 Create a `bd` issue documenting Phase 3 rules (`ComplexMethod`, `CyclomaticComplexMethod`, `SwallowedException`, `TooGenericExceptionCaught`) and the code refactoring needed to enable them
- [ ] 6.2 Link the issue as a `discovered-from` dependency of this change
