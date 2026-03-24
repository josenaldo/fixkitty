# US-010 — Sudo Privilege Manager

**Epic:** Infrastructure
**Phase:** 1
**Status:** Pending

## Story

**As a** system,
**I want** a `SudoPrivilegeManager` that prepends `sudo` to privileged commands,
**so that** recovery actions requiring elevated privileges work in the TUI.

## Dependencies

- **US-005** — Port Interfaces — `PrivilegeManager` must exist

## Acceptance Criteria

- `escalate(String[] command)` returns a new array with `"sudo"` prepended
- The original input array is not mutated
- Unit tests pass

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.infrastructure.privilege.SudoPrivilegeManager` implements `PrivilegeManager`

### Key Behavior

```java
@Override
public String[] escalate(String[] command) {
    String[] escalated = new String[command.length + 1];
    escalated[0] = "sudo";
    System.arraycopy(command, 0, escalated, 1, command.length);
    return escalated;
}
```

Example:

- Input: `["systemctl", "restart", "bluetooth"]`
- Output: `["sudo", "systemctl", "restart", "bluetooth"]`

### Unit Tests to Write

- `escalate_prependsSudo()` — verify the first element of the returned array is `"sudo"` and the remaining elements match the input
- `escalate_emptyCommand_returnsSudoOnly()` — input `[]`; output `["sudo"]`
- `escalate_doesNotMutateOriginalArray()` — verify the input array is unchanged after the call

### Edge Cases

- An empty input array is a valid input; the output must be `["sudo"]` and must not throw
- The method must not prepend `"sudo"` twice if the input already starts with `"sudo"`; however, detecting this is not required for Phase 1 (the use case is responsible for not calling `escalate` on already-escalated commands)

## Related

- **ADR-004** — Privilege escalation strategy; `sudo` is selected for TUI and `pkexec` for GUI
- **TC-TUI-005** — TUI test that verifies privileged steps use sudo
