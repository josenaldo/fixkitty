# ADR-007: Fix GNOME Shell — Warn, Execute, and Relaunch

**Date:** 2026-03-23
**Status:** Accepted

## Context

The "Fix GNOME Shell" action executes `killall gnome-shell`, which terminates the graphical
session manager. When triggered from the GUI, this closes the window in which FixKitty
itself is running.

Without mitigation, the user would click "Fix GNOME Shell" and the application would
silently disappear mid-execution, leaving no feedback and requiring a manual relaunch.

## Decision

The GUI applies the following flow for Fix GNOME Shell:

1. Display a confirmation dialog warning the user that the graphical interface will
   briefly close and that FixKitty will relaunch automatically.
2. If the user confirms, execute the action.
3. After execution, `AppRestarter` relaunches FixKitty as a new process via
   `ProcessBuilder` and then terminates the current JVM process.

The TUI is not affected — it runs in a terminal and is not disrupted by killing the
GNOME shell process.

`AppRestarter` lives in `interfaces/gui` and is not part of the domain or application
layers.

## Consequences

- Users are never surprised by the application closing without explanation
- The application returns automatically after GNOME shell restarts (typically 2–5 seconds)
- `AppRestarter` must be tested manually — automated testing of process relaunch is
  impractical in unit tests
