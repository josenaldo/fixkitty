# US-012 — Ubuntu 24 Environment Detector

**Epic:** Infrastructure
**Phase:** 1
**Status:** Pending

## Story

**As a** system,
**I want** an `Ubuntu24EnvironmentDetector` that detects distro, desktop, audio stack, and graphical session,
**so that** the correct action catalog is selected.

## Dependencies

- **US-004** — Domain Entities — `EnvironmentProfile` must exist
- **US-005** — Port Interfaces — `EnvironmentDetector` must exist

## Acceptance Criteria

- `detect()` returns a fully populated `EnvironmentProfile`
- `distro` is read from `/etc/os-release`; defaults to `"Unknown"` on failure
- `desktop` is read from `XDG_CURRENT_DESKTOP`; defaults to `"Unknown"` if absent
- `audioStack` is detected by querying `systemctl --user is-active pipewire`; defaults to `"Unknown"` on failure
- `hasGraphicalSession` is `true` if `DISPLAY` or `WAYLAND_DISPLAY` is set
- Unit tests cover all default/fallback cases

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.infrastructure.detectors.Ubuntu24EnvironmentDetector` implements `EnvironmentDetector`

### Key Behavior

The `detect()` method executes four independent detections and assembles the result:

#### 1. Distro Detection

Read `/etc/os-release` line by line. Find the line that starts with `PRETTY_NAME=`. Strip the `PRETTY_NAME=` prefix and remove surrounding double quotes.

```java
private String detectDistro() {
    try {
        return Files.lines(Path.of("/etc/os-release"))
            .filter(line -> line.startsWith("PRETTY_NAME="))
            .findFirst()
            .map(line -> line.substring("PRETTY_NAME=".length()).replace("\"", "").trim())
            .orElse("Unknown");
    } catch (IOException e) {
        log.warn("Could not read /etc/os-release: {}", e.getMessage());
        return "Unknown";
    }
}
```

#### 2. Desktop Detection

```java
private String detectDesktop() {
    String desktop = System.getenv("XDG_CURRENT_DESKTOP");
    return (desktop != null && !desktop.isBlank()) ? desktop : "Unknown";
}
```

#### 3. Audio Stack Detection

Run `["systemctl", "--user", "is-active", "pipewire"]` using a direct `ProcessBuilder` call (not via `CommandRunner`, to avoid circular dependency):

```java
private String detectAudioStack() {
    try {
        Process process = new ProcessBuilder("systemctl", "--user", "is-active", "pipewire")
            .start();
        boolean finished = process.waitFor(5, TimeUnit.SECONDS);
        if (finished && process.exitValue() == 0) {
            return "PipeWire";
        }
    } catch (IOException | InterruptedException e) {
        log.warn("Could not detect audio stack: {}", e.getMessage());
    }
    return "Unknown";
}
```

#### 4. Graphical Session Detection

```java
private boolean detectGraphicalSession() {
    return System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null;
}
```

#### Assembly

```java
@Override
public EnvironmentProfile detect() {
    return new EnvironmentProfile(
        detectDistro(),
        detectDesktop(),
        detectAudioStack(),
        detectGraphicalSession()
    );
}
```

### Edge Cases

- `/etc/os-release` not found → `distro = "Unknown"`; the exception is caught and logged at WARN level
- `systemctl` not installed or not found in `PATH` → `audioStack = "Unknown"`; `IOException` is caught and logged
- Audio stack detection times out after 5 seconds → `audioStack = "Unknown"`; the process is not waited on indefinitely
- Running in a TTY (no `DISPLAY` or `WAYLAND_DISPLAY`) → `hasGraphicalSession = false`
- `XDG_CURRENT_DESKTOP` is set but blank → `desktop = "Unknown"`

## Related

- **ADR-005** — Environment detection strategy; this US implements it for Ubuntu 24
- **TC-GUI-010** — GUI test that verifies the environment panel shows the detected profile
- **TC-TUI-010** — TUI test that verifies the environment screen shows the detected profile
