# Fixkitty — Recovery Console for Linux Desktop

Java 25 + JavaFX + Clean Architecture. Multi-interface desktop recovery tool (GUI + TUI) for Linux subsystems (audio, network, Bluetooth, GNOME shell).

## Mandatory Rules

These rules apply to ALL agents, skills, and contributors. No exceptions.

### Language

- **All repository content must be written in English**: code, comments, Javadoc, commit messages, documentation, ADRs, specs, plans, and PR descriptions.
- Conversation with the user may happen in Portuguese or English, but everything that enters the repository must be in English.

### Javadoc

- **All Java types, methods, and fields must have Javadoc comments.**
- Javadoc must describe purpose, parameters, return values, and exceptions where applicable.
- Do not write trivial or redundant Javadoc — each comment must add value.

### Documentation Structure

All project documentation lives under `docs/`:

```text
docs/
  architecture/   ← architectural overviews, diagrams, and design specs
  decisions/      ← ADRs (Architecture Decision Records)
  plans/          ← implementation plans
  user-stories/   ← user story documents
  tests/          ← manual and future automated test cases (GUI, TUI)
```

Naming conventions:

- ADRs: `ADR-NNN-short-title.md`
- Plans: `YYYY-MM-DD-<topic>-plan.md`
- Design specs: `YYYY-MM-DD-<topic>-design.md` (stored in `docs/architecture/`)
- User stories: `US-NNN-short-title.md` or grouped by epic
- Test cases: `<interface>-test-cases.md` (stored in `docs/tests/`)

## Architecture

- **Domain**: `src/main/java/br/com/josenaldo/fixkitty/core` — recovery actions, environment profiles, execution plans
- **Application**: `src/main/java/br/com/josenaldo/fixkitty/application` — use cases, recovery orchestration
- **Infrastructure**: `src/main/java/br/com/josenaldo/fixkitty/infrastructure` — system commands, privilege escalation, profiles
- **Interfaces**: `src/main/java/br/com/josenaldo/fixkitty/interfaces/gui` (JavaFX) + `.../tui` (Lanterna) — UI only, no business logic
- **Bootstrap**: `src/main/java/br/com/josenaldo/fixkitty/bootstrap` — Main, AppModule (Guice), InterfaceSelector

## Architecture Rules

- NEVER put business logic in GUI or TUI controllers
- NEVER import `interfaces.*` classes in `core` or `application`
- ALWAYS delegate command execution to the infrastructure layer via ports
- NEVER hardcode `systemctl`/`sudo` commands outside `infrastructure/` — use `EnvironmentProfile`
- NEVER import Lanterna classes outside `interfaces/tui`
- ALWAYS write tests for every new Use Case (no GUI/TUI required)

## Commands

- `./gradlew build` — Compile the project
- `./gradlew test` — Run unit tests
- `./gradlew run` — Launch GUI
- `./gradlew run --args="--tui"` — Launch TUI

## Skills

See `.agents/skills/` for detailed procedures:

- `layer-domain` — Core/domain layer modeling
- `layer-application` — Use case orchestration
- `layer-infrastructure` — Adapters, profiles, and Linux integrations
- `layer-interface` — JavaFX GUI, TUI, and bootstrap entry points
- `write-tests` — Testing strategy per layer
- `implement-feature` — Full end-to-end implementation cycle
- `create-recovery-action` — Define a recovery action
- `create-use-case` — Create an execution orchestrator
- `create-environment-profile` — New distro/desktop profile
- `create-ui-component` — JavaFX or TUI controller
- `write-unit-test` — Unit tests for Use Cases and domain
- `enforce-architecture` — Validate Clean Architecture boundaries (mandatory final step)

## Memory

See `memory/MEMORY.md` for architectural decisions and confirmed patterns.

## Fallback

If a referenced file does not exist, report the blocker and document the assumption made.
