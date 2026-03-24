# ADR-002: Google Guice for Dependency Injection

**Date:** 2026-03-23
**Status:** Accepted

## Context

Clean Architecture requires that layers are wired together without hard dependencies.
As the number of use cases, ports, and implementations grows, manual wiring in a factory
class becomes verbose and error-prone.

A DI container is needed that works well in a desktop (non-web) context, is lightweight,
and integrates naturally with Java 25.

## Decision

Use **Google Guice** as the dependency injection container.

- All wiring is centralized in `bootstrap/AppModule`
- Classes declare dependencies via `@Inject`
- The correct `PrivilegeManager` implementation (Sudo vs Pkexec) is bound conditionally
  depending on the selected interface (TUI or GUI)

Spring Boot, Micronaut, and Quarkus were considered and rejected — all are web-server
oriented and would add unnecessary weight to a desktop application.

Dagger 2 was considered but rejected due to higher setup complexity with Java 25 annotation
processing and steeper learning curve relative to the project's scope.

## Consequences

- Wiring errors surface at runtime startup (not compile time)
- Swapping implementations (e.g., replacing `SudoPrivilegeManager`) requires only a
  one-line change in `AppModule`
- Guice modules can be split per layer as the project grows
