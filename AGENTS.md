# Fixkitty — Recovery Console for Linux Desktop

Java 25 + JavaFX + Clean Architecture. Multi-interface desktop recovery tool (GUI + TUI) for Linux subsystems (audio, network, Bluetooth, GNOME shell).

## Architecture

- **Domain**: `src/main/java/org/fixkitty/core` — recovery actions, environment profiles, execution plans
- **Application**: `src/main/java/org/fixkitty/application` — use cases, recovery orchestration
- **Infrastructure**: `src/main/java/org/fixkitty/infrastructure` — system commands, privilege escalation, profiles
- **Interfaces**: `src/main/java/org/fixkitty/interfaces/gui` (JavaFX) + `src/main/java/org/fixkitty/interfaces/tui` (terminal) — UI only, no business logic

## Rules

- NUNCA coloque lógica de domínio em GUI ou TUI controllers
- NUNCA importe classes de interfaces (gui/tui) em core ou application
- SEMPRE delegue execução de comandos para infrastructure layer via ports
- NUNCA condicione comportamento a `systemctl`/`sudo` específicos — use EnvironmentProfile
- SEMPRE escreva testes para novo Use Case (sem GUI/TUI)

## Commands

- `./gradlew build` — Compilar projeto
- `./gradlew test` — Rodar testes unitários
- `./gradlew run` — Executar GUI
- `./gradlew run --args="--tui"` — Executar TUI

## Skills

Ver `.agents/skills/` para procedimentos detalhados:
- `create-recovery-action` — Definir ação de reset
- `create-use-case` — Criar orquestrador de execução
- `create-environment-profile` — Novo profile de distro/desktop
- `create-ui-component` — Controller JavaFX ou TUI
- `enforce-architecture` — Validar fronteiras Clean Architecture

## Memory

Ver `memory/MEMORY.md` para decisões arquiteturais e padrões confirmados.

## Fallback

Se um arquivo referenciado não existir, reportar bloqueio e registrar suposição feita.
