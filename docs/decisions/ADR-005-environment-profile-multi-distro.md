# ADR-005: EnvironmentProfile for Multi-Distro Support

**Date:** 2026-03-23
**Status:** Accepted

## Context

Linux distributions differ significantly in service names, audio stacks, desktop
environments, and privilege mechanisms. For example:

- Ubuntu 24 uses `pipewire` and `wireplumber` for audio; older systems may use `pulseaudio`
- Fedora and Ubuntu have different service names for the same subsystems
- KDE and GNOME require different commands for shell restart

Hardcoding Ubuntu-specific commands into use cases would block multi-distro support
and create a tangle of conditionals over time.

## Decision

Introduce `EnvironmentProfile` as a domain concept. Concrete commands **never** appear
in use cases or domain entities — only in `infrastructure/catalog` implementations.

- `EnvironmentDetector` (port) detects the current environment and returns an `EnvironmentProfile`
- `ActionCatalog` (port) returns the correct `ExecutionStep` list for a given action
  and profile
- `Ubuntu24ActionCatalog` is the Phase 1 implementation; future distros add their own
  catalog without changing any existing code

## Consequences

- Adding support for a new distro requires only a new `EnvironmentProfile` implementation
  and a new `ActionCatalog` — zero changes to core or application
- Phase 1 is intentionally scoped to Ubuntu 24 + GNOME + PipeWire + NetworkManager
- The architecture is ready for multi-distro from day one without over-engineering
