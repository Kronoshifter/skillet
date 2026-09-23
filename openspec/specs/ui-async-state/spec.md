# ui-async-state Specification

## Purpose
Defines how the UI layer models and renders an asynchronous data lifecycle. It specifies the exhaustive set of states a screen can be in, how each state is displayed, how a ViewModel holds and updates that state, and the rule that a ViewModel is used only at the top level of a screen (children receive plain data + lambdas, never the ViewModel).

## Requirements

### Requirement: The async state SHALL model four mutually exclusive lifecycle states

The async data state SHALL be an exhaustive sealed type with exactly four states: an initial not-yet-started (idle) state, an actively-loading state, a success state that carries the data, and a failure state that carries an error. Only the success state SHALL carry the data value; only the failure state SHALL carry an error. The four states SHALL be mutually exclusive and jointly exhaustive for consumers.

#### Scenario: Success is the only data-carrying state
- **WHEN** the async state holds loaded data
- **THEN** the state is the success state, exposes the data value, and exposes no error and a non-loading indicator

#### Scenario: Failure is the only error-carrying state
- **WHEN** loading a resource fails
- **THEN** the state is the failure state, exposes the error, and exposes no data

#### Scenario: The state space is exhaustive
- **WHEN** a consumer branches over the async state
- **THEN** handling the idle, loading, success, and failure states is required to cover the type completely

### Requirement: The async state SHALL expose direct read accessors

The async state SHALL expose the data value, the error value, and an "is loading" flag as direct reads so a consumer does not need to branch in order to inspect the state.

#### Scenario: Reading data from a non-success state yields none
- **WHEN** the data value is read while the state is idle, loading, or failure
- **THEN** it yields no data

#### Scenario: The loading flag reflects only the loading state
- **WHEN** the "is loading" flag is read
- **THEN** it is true only while the state is loading and false while idle, in success, and in failure

### Requirement: A data screen SHALL begin idle and show a placeholder until the first success

A screen that loads data asynchronously SHALL begin in the idle state and SHALL render a loading placeholder (not a blank) until the first success arrives, so a fast cold load does not flash an empty screen.

#### Scenario: Cold start shows a placeholder, not a blank
- **WHEN** a data screen is first composed before any data has loaded
- **THEN** a loading placeholder is shown until the first success arrives

#### Scenario: A success replaces the placeholder with content
- **WHEN** the first loaded value arrives
- **THEN** the placeholder is replaced by the screen's loaded content

### Requirement: A data screen SHALL retain the last loaded value for its lifetime

The loaded value of a data screen SHALL persist for the lifetime of the screen's ViewModel regardless of whether the screen is currently observing it, so leaving and re-entering a screen does not discard the value or re-trigger a load placeholder.

#### Scenario: Re-entering a loaded screen shows the retained value
- **WHEN** a screen that has already loaded data is left and then re-entered within the same ViewModel lifetime
- **THEN** the last loaded value is shown without a fresh load placeholder

### Requirement: A load failure SHALL be surfaced and retained as a failure

When a data screen's data source fails, the screen SHALL transition to the failure state carrying the error and SHALL retain that failure until a later successful emission replaces it; a failure SHALL NOT silently revert to a blank or to a fresh load.

#### Scenario: A load failure is shown as an error
- **WHEN** the data source fails while a data screen is loading
- **THEN** the screen shows the failure state carrying an error

#### Scenario: A failure persists until replaced
- **WHEN** a screen in the failure state is re-entered
- **THEN** the failure is still shown (not a blank or a new load) until a successful emission occurs

### Requirement: Async data SHALL be rendered through a single full-wrap content contract

The UI SHALL provide a single full-wrap content contract that renders the correct content for each async state: a placeholder for the idle state, a placeholder for the loading state, an error view for the failure state, and the caller-supplied content for the success state. Transitions between these states SHALL be animated. The idle and loading placeholders and the error view SHALL each have sensible defaults that a caller MAY override.

#### Scenario: Each state renders its own content
- **WHEN** the async state is success, failure, loading, or idle
- **THEN** the contract renders the caller's success content, an error view, a loading placeholder, or a loading placeholder respectively

#### Scenario: A default error view is shown when none is supplied
- **WHEN** the state is failure and the caller supplies no custom error view
- **THEN** a default error view showing the error's message is rendered

### Requirement: A form screen's initial load or scrape SHALL be driven by a plain busy flag

A form screen that performs an initial load or scrape SHALL expose that operation as a boolean busy signal rather than as the async data state. While busy the screen SHALL show a busy placeholder; when not busy it SHALL show the form. This initial-load busy signal SHALL be separate from the save flow, which SHALL indicate its own in-progress state without replacing the form.

#### Scenario: During an initial load or scrape the form is replaced by a placeholder
- **WHEN** the form screen's initial load or scrape is in progress
- **THEN** a busy placeholder is shown in place of the form

#### Scenario: When not busy the form is shown
- **WHEN** the form screen is not in an initial load or scrape
- **THEN** the form is shown

#### Scenario: Saving does not tear down the form
- **WHEN** the form screen is saving
- **THEN** the in-progress state is indicated without the form being replaced, so the form's interactive state is preserved

### Requirement: A ViewModel SHALL be used only at the top level of its screen

A screen's ViewModel SHALL be accessed only within the top-level composable function of that screen. The top-level composable SHALL collect the screen's states (including the async data state and any high-churn interaction state such as scaling or unit selection) and SHALL pass to child composables only plain data values and callback lambdas (for example method references to ViewModel functions). A child composable SHALL NOT receive the ViewModel instance as a parameter.

#### Scenario: Child composables never receive the ViewModel
- **WHEN** a screen's top-level composable passes screen state into a child content composable
- **THEN** it passes collected plain values and method-reference lambdas, and no child composable in the screen takes a ViewModel parameter

#### Scenario: Both async and interaction state are collected at the top level
- **WHEN** a screen displays both async data and frequently-changing interaction state
- **THEN** both states are collected within the top-level composable, and a change to the interaction state may re-compose the top level including the async content wrapper (accepted trade-off, in exchange for the top-level ViewModel rule)

#### Scenario: A data state change re-composes through the async wrapper
- **WHEN** the async data transitions between states (for example from idle to success)
- **THEN** the async content wrapper re-executes to render the new state
