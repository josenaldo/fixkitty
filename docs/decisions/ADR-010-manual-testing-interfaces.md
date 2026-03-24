# ADR-010: Manual Testing for GUI and TUI in Phase 1 (Automation Deferred)

**Date:** 2026-03-23
**Status:** Accepted

## Context

Clean Architecture requires tests for all use cases and domain logic. However, the GUI
(JavaFX) and TUI (Lanterna) layers are thin adapters with no business logic — they call
use cases and render results. Automated testing of these layers has a different cost/benefit
profile than testing the core.

Automation is technically feasible for both layers:

- **JavaFX GUI**: TestFX + Monocle (JavaFX headless renderer). TestFX allows programmatic
  clicks, assertions on UI state, and runs on CI without a real display.
- **Lanterna TUI**: Lanterna provides `VirtualTerminal` and `DefaultTerminalFactory` for
  in-memory terminal simulation, enabling input injection and output capture.

## Decision

In Phase 1, GUI and TUI are tested **manually only**. All test cases are documented in
`docs/tests/` to enable future automation without re-discovery.

Reasons for deferring automation to Phase 2+:

- GUI controllers contain no business logic — correctness is verified visually, not logically
- TestFX + Monocle setup has non-trivial CI configuration overhead for a Phase 1 MVP
- Lanterna's virtual terminal API is less ergonomic than TestFX; manual testing is faster here
- Interface shapes will stabilize in Phase 1; automating before stabilization wastes effort

## Future Automation Path (Phase 2+)

When the interface layer stabilizes, add:

- **TestFX** (`org.testfx:testfx-junit5`) + **Monocle** (`org.openjfx:javafx-graphics:monocle`)
  for headless JavaFX tests in CI
- **Lanterna VirtualTerminal** for TUI flow tests once the menu structure is stable
- Test case documents in `docs/tests/` already contain structured scenarios ready for automation

## Consequences

- Use cases and domain entities have full unit test coverage
- GUI/TUI correctness is verified through manual test scenarios in `docs/tests/`
- AGENTS.md rule "ALWAYS write tests for every new Use Case" applies to `application/` only,
  not to `interfaces/`
- When automation is added, test cases in `docs/tests/` serve as the specification for each test method
