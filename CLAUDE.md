# Fixkitty Recovery Console

Java 25 + Gradle, Clean Architecture (multi-interface: JavaFX + TUI).

## Architecture

- **Domain** (`core/`): RecoveryAction, EnvironmentProfile, ExecutionResult
- **Application** (`application/`): RecoveryOrchestrator, Use Cases
- **Infrastructure** (`infrastructure/`): CommandRunner, PrivilegeManager, LinuxProfiles
- **Interfaces**: GUI (JavaFX) + TUI (terminal) — no business logic

## Rules

- NUNCA lógica de negócio em controllers (GUI/TUI)
- NUNCA imports circulares entre camadas
- SEMPRE use abstrato (ports) em infrastructure
- NUNCA hardcode comandos Linux — usar EnvironmentProfile

## Commands

- `./gradlew build` — compile
- `./gradlew test` — unit tests
- `./gradlew run` — GUI
- `./gradlew run --args="--tui"` — TUI

## Skills

Ver AGENTS.md para lista e `.agents/skills/` para procedimentos.

## Memory

Ver `memory/MEMORY.md` para decisões arquiteturais.
