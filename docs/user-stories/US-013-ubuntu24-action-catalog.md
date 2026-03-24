# US-013 — Ubuntu 24 Action Catalog

**Epic:** Infrastructure
**Phase:** 1
**Status:** Pending

## Story

**As a** system,
**I want** an `Ubuntu24ActionCatalog` with concrete `ExecutionStep` lists for all 6 recovery actions,
**so that** the correct commands are used on Ubuntu 24.

## Dependencies

- **US-004** — Domain Entities — `RecoveryAction`, `ExecutionStep`, `FailurePolicy` must exist
- **US-005** — Port Interfaces — `ActionCatalog` must exist
- **US-012** — Ubuntu 24 Environment Detector — the catalog is designed for the same target environment

## Acceptance Criteria

- `actionsFor(profile)` returns all 6 `RecoveryAction` values
- `planFor` returns the correct steps for each action as specified below
- `planFor(CHECK_ENVIRONMENT, ...)` returns an empty list
- `defaultRecommendationFor` returns the correct string for each action
- Unit tests pass

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.infrastructure.catalog.Ubuntu24ActionCatalog` implements `ActionCatalog`

### Key Behavior

#### actionsFor

```java
@Override
public List<RecoveryAction> actionsFor(EnvironmentProfile profile) {
    return List.of(
        RecoveryAction.FIX_AUDIO,
        RecoveryAction.FIX_BLUETOOTH,
        RecoveryAction.FIX_NETWORK,
        RecoveryAction.FIX_GNOME_SHELL,
        RecoveryAction.FIX_ALL,
        RecoveryAction.CHECK_ENVIRONMENT
    );
}
```

#### planFor

The `profile` parameter is accepted for future multi-distro support but is not used in Phase 1.

**FIX_AUDIO** — 2 steps:

| Field | Step 1 | Step 2 |
| --- | --- | --- |
| id | `audio-pipewire` | `audio-wireplumber` |
| description | Restart PipeWire | Restart WirePlumber |
| command | `["systemctl","--user","restart","pipewire"]` | `["systemctl","--user","restart","wireplumber"]` |
| requiresPrivilege | false | false |
| timeoutSeconds | 10 | 10 |
| onFailure | CONTINUE | CONTINUE |

**FIX_BLUETOOTH** — 1 step:

| Field | Step 1 |
| --- | --- |
| id | `bt-restart` |
| description | Restart Bluetooth service |
| command | `["systemctl","restart","bluetooth"]` |
| requiresPrivilege | true |
| timeoutSeconds | 15 |
| onFailure | ABORT |

**FIX_NETWORK** — 1 step:

| Field | Step 1 |
| --- | --- |
| id | `net-restart` |
| description | Restart NetworkManager |
| command | `["systemctl","restart","NetworkManager"]` |
| requiresPrivilege | true |
| timeoutSeconds | 20 |
| onFailure | ABORT |

**FIX_GNOME_SHELL** — 1 step:

| Field | Step 1 |
| --- | --- |
| id | `gnome-killall` |
| description | Restart GNOME Shell |
| command | `["killall","gnome-shell"]` |
| requiresPrivilege | false |
| timeoutSeconds | 10 |
| onFailure | ABORT |

**FIX_ALL** — 5 steps, combining all individual steps in order, but all with `onFailure = CONTINUE`:

| Order | id | description |
| --- | --- | --- |
| 1 | `audio-pipewire` | Restart PipeWire |
| 2 | `audio-wireplumber` | Restart WirePlumber |
| 3 | `bt-restart` | Restart Bluetooth service |
| 4 | `net-restart` | Restart NetworkManager |
| 5 | `gnome-killall` | Restart GNOME Shell |

All five steps in `FIX_ALL` override their individual `onFailure` policy to `CONTINUE`. The other fields (command, requiresPrivilege, timeoutSeconds) are identical to the individual action steps.

**CHECK_ENVIRONMENT** — returns `List.of()` (empty list).

#### defaultRecommendationFor

| Action | Recommendation |
| --- | --- |
| FIX_AUDIO | `"Try logging out and logging back in if audio is still broken."` |
| FIX_BLUETOOTH | `"Try toggling Bluetooth off and on in system settings."` |
| FIX_NETWORK | `"Try disconnecting and reconnecting to your network."` |
| FIX_GNOME_SHELL | `"If the issue persists, try a full logout and login."` |
| FIX_ALL | `"If issues persist, consider a full logout or system restart."` |
| CHECK_ENVIRONMENT | `null` |

### Unit Tests to Write

- `planFor_fixAudio_returnsTwoSteps()` — assert the list has 2 elements with the correct ids and onFailure=CONTINUE
- `planFor_fixAll_returnsAllStepsWithContinuePolicy()` — assert 5 steps, all with `onFailure == CONTINUE`
- `planFor_checkEnvironment_returnsEmptyList()` — assert the list is empty
- `defaultRecommendationFor_allActions_nonNullExceptCheckEnvironment()` — iterate all enum values; assert non-null for all except `CHECK_ENVIRONMENT`

### Edge Cases

- `FIX_ALL` steps must each have `onFailure = CONTINUE` regardless of what the individual action plans use; create fresh `ExecutionStep` instances for `FIX_ALL` rather than reusing references from the individual plan methods
- The `profile` parameter in `planFor` and `actionsFor` must not cause a `NullPointerException` if `null` is passed; defensive null check is acceptable for Phase 1

## Related

- **ADR-005** — Environment-specific catalog design
- **ADR-006** — Step execution semantics including `FailurePolicy`
- **TC-GUI-002** — GUI test for audio fix execution
- **TC-GUI-008** — GUI test for FIX_ALL execution
- **TC-TUI-003** — TUI test for audio fix execution
- **TC-TUI-008** — TUI test for FIX_ALL execution
