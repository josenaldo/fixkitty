# US-003 — Bootstrap and Entry Point

**Epic:** Project Setup
**Phase:** 1
**Status:** Pending

## Story

**As a** developer,
**I want** a `Main.java` that selects GUI or TUI based on the `--tui` argument, backed by a Guice `AppModule`,
**so that** the entry point is operational and wiring is centralized.

## Dependencies

- **US-001** — Gradle Project Setup — Guice and Logback must be on the classpath
- **US-002** — Package Structure — the `bootstrap` package must exist before classes are placed in it

## Acceptance Criteria

- `./gradlew run` launches the GUI (a stub window or log message is acceptable for this story)
- `./gradlew run --args="--tui"` launches the TUI (a stub screen or log message is acceptable for this story)
- `AppModule` exists and binds at least one component without error
- `logback.xml` is configured with a console appender and a rolling file appender writing to `~/.fixkitty/logs/`
- If no graphical session is detected at startup, the app falls back to TUI automatically with a warning log

## Implementation Notes

### Classes to Create

- `br.com.josenaldo.fixkitty.bootstrap.Main` — reads `args`, creates a Guice injector with `AppModule`, delegates to `InterfaceSelector`
- `br.com.josenaldo.fixkitty.bootstrap.InterfaceSelector` — decides between GUI and TUI based on runtime conditions
- `br.com.josenaldo.fixkitty.bootstrap.AppModule` — extends `com.google.inject.AbstractModule`; empty bindings for this story, to be expanded in later stories

### Key Behavior

`Main.main(String[] args)` must:

1. Create the Guice injector: `Guice.createInjector(new AppModule())`
2. Obtain an `InterfaceSelector` instance from the injector
3. Call `interfaceSelector.launch(args)`
4. Call `System.exit(0)` on clean shutdown

`InterfaceSelector.launch(String[] args)` must apply this decision logic in order:

1. If `args` contains `"--tui"` → launch TUI
2. Else if both `System.getenv("DISPLAY")` and `System.getenv("WAYLAND_DISPLAY")` are null or blank → launch TUI with a warning logged via SLF4J: `"No graphical session detected; falling back to TUI"`
3. Else → attempt GUI launch; if launch throws any exception (e.g., `UnsatisfiedLinkError` when JavaFX native libs are missing), log the exception at WARN level and fall back to TUI

`GuiStub` and `TuiStub` placeholder classes in their respective interface packages are acceptable for this story. Each stub must log a single INFO message at startup, for example: `"GUI started (stub)"` or `"TUI started (stub)"`.

`AppModule` initial bindings (expand in later stories):

```java
@Override
protected void configure() {
    // Bindings added incrementally as infrastructure is implemented
}
```

`logback.xml` rolling file configuration:

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${user.home}/.fixkitty/logs/fixkitty.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${user.home}/.fixkitty/logs/fixkitty.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>7</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

### Edge Cases

- If the GUI launch throws `UnsatisfiedLinkError` (missing JavaFX native libraries on headless systems), this must be caught and handled as a fallback to TUI, not allowed to propagate as an uncaught exception
- `System.exit(0)` must be called after `Platform.exit()` if the GUI path was taken, to ensure the JavaFX application thread terminates cleanly
- The log directory `~/.fixkitty/logs/` will be created automatically by Logback's rolling appender if it does not exist; no manual `Files.createDirectories()` call is needed

## Related

- **ADR-001** — Clean Architecture; `bootstrap` is the only layer allowed to depend on all other layers
- **ADR-002** — Guice selected as the DI framework; `AppModule` is the single Guice module for this project
