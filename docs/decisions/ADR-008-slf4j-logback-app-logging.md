# ADR-008: SLF4J + Logback for Application Logging

**Date:** 2026-03-23
**Status:** Accepted

## Context

Two distinct types of "logging" exist in FixKitty:

1. **Application logging** — diagnostic output for developers: debug traces, errors,
   startup info. This is internal and not shown to the end user.
2. **Execution logging** — structured output from running recovery commands: stdout,
   stderr, exit codes, durations. This is user-facing and lives in `StepResult`.

Mixing these two concerns would couple domain objects to a logging framework and make
it harder to render execution output in the GUI and TUI.

## Decision

- **Application logging**: SLF4J (facade) + Logback (implementation). Used in
  infrastructure and bootstrap layers for diagnostic output. Never used in `core/domain`.
- **Execution logging**: Captured as fields in `StepResult` (stdout, stderr, exitCode,
  durationMs, friendlyMessage). Rendered by interfaces (GUI/TUI). No logging framework involved.

Log4j2 was considered but rejected — its performance advantage is irrelevant for a
desktop app, and its security history (Log4Shell, 2021) adds unnecessary concern.

`java.util.logging` was rejected due to its verbose API and limited configuration options.

## Consequences

- Domain entities have zero dependency on any logging framework
- Interfaces format `StepResult` fields freely for their respective rendering context
- Logback configuration lives in `src/main/resources/logback.xml`
- Log files are written to a configurable path (default: `~/.fixkitty/logs/`)
