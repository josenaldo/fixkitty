# US-011 — Pkexec Privilege Manager

**Epic:** Infrastructure
**Phase:** 1
**Status:** Pending

## Story

**As a** system,
**I want** a `PkexecPrivilegeManager` that wraps commands with `pkexec`,
**so that** the GUI shows a native authentication dialog for privileged actions.

## Dependencies

- **US-005** — Port Interfaces — `PrivilegeManager` must exist

## Acceptance Criteria

- `escalate(String[] command)` returns a new array with `"pkexec"` prepended
- The original input array is not mutated
- Unit tests pass

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.infrastructure.privilege.PkexecPrivilegeManager` implements `PrivilegeManager`

### Key Behavior

```java
@Override
public String[] escalate(String[] command) {
    String[] escalated = new String[command.length + 1];
    escalated[0] = "pkexec";
    System.arraycopy(command, 0, escalated, 1, command.length);
    return escalated;
}
```

Example:

- Input: `["systemctl", "restart", "NetworkManager"]`
- Output: `["pkexec", "systemctl", "restart", "NetworkManager"]`

When the GUI calls a privileged step, `pkexec` will present a native PolicyKit authentication dialog to the user before executing the command. This happens transparently at the OS level; no additional GUI code is required to show the dialog.

### Unit Tests to Write

- `escalate_prependsPkexec()` — verify the first element of the returned array is `"pkexec"` and the remaining elements match the input
- `escalate_emptyCommand_returnsPkexecOnly()` — input `[]`; output `["pkexec"]`
- `escalate_doesNotMutateOriginalArray()` — verify the input array is unchanged after the call

### Edge Cases

- An empty input array is valid; the output must be `["pkexec"]` and must not throw
- `pkexec` requires a PolicyKit policy file to be installed for the target executable; if the policy is missing, the process will exit with a non-zero code. This is reported as `StepStatus.FAILED` by the `CommandRunner` and is not the responsibility of this class to handle
- On systems where `pkexec` is not installed, the process start will fail with `IOException`; this is handled by `ProcessBuilderCommandRunner`, not by this class

## Related

- **ADR-004** — Privilege escalation strategy; `pkexec` is selected for the GUI path
- **TC-GUI-004** — GUI test that verifies privileged steps trigger the pkexec dialog
