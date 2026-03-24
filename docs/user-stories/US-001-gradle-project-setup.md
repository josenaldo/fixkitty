# US-001 — Gradle Project Setup

**Epic:** Project Setup
**Phase:** 1
**Status:** Pending

## Story

**As a** developer,
**I want** a Gradle project configured with Java 25, Guice, SLF4J+Logback, JavaFX 23, AtlantaFX, Ikonli, Lanterna 3.x, JUnit 5, and Mockito,
**so that** I can begin implementing without manual configuration.

## Dependencies

None.

## Acceptance Criteria

- `./gradlew build` compiles with no errors
- All declared dependencies resolve from Maven Central
- Java 25 toolchain configured in `build.gradle.kts`
- `settings.gradle.kts` has root project name `fixkitty`

## Implementation Notes

### Files to Modify

- `build.gradle.kts` — add all dependencies with versions
- `settings.gradle.kts` — set rootProject.name = "fixkitty"

### Key Behavior

The build file must declare these dependency groups:

- `com.google.inject:guice:7.0.0`
- `org.slf4j:slf4j-api:2.0.x` + `ch.qos.logback:logback-classic:1.5.x`
- `org.openjfx:javafx-controls:23` + `org.openjfx:javafx-fxml:23` (with javafx plugin)
- `io.github.mkpaz:atlantafx-base:2.0.1`
- `org.kordamp.ikonli:ikonli-javafx:12.3.1` + `org.kordamp.ikonli:ikonli-fontawesome5-pack:12.3.1`
- `com.googlecode.lanterna:lanterna:3.1.2`
- `org.junit.jupiter:junit-jupiter:5.11.x` (via BOM)
- `org.mockito:mockito-core:5.x`
- `org.testfx:testfx-junit5:4.0.18` + `org.openjfx:javafx-graphics:monocle` (testImplementation, for future use)

The Java toolchain block must explicitly request Java 25:

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

The JavaFX plugin must be applied via the plugin id `org.openjfx.javafxplugin` and configured with the modules `javafx.controls` and `javafx.fxml`.

A `logback.xml` file must be created at `src/main/resources/logback.xml` with at least a console appender and a rolling file appender writing to `${user.home}/.fixkitty/logs/fixkitty.log`.

### Edge Cases

- Java 25 toolchain must be declared explicitly; relying on the default JVM is not acceptable
- The JavaFX plugin `org.openjfx.javafxplugin` is required for correct module resolution; without it JavaFX classes will not be found at runtime
- `logback.xml` must be present in `src/main/resources/` before any class that uses SLF4J is executed, or a `No SLF4J providers were found` warning will appear at startup

## Related

- **ADR-002** — Guice selected as the DI framework; this US declares the dependency
- **ADR-008** — Logback selected as the logging backend; this US sets up the configuration file
- **ADR-009** — JavaFX + AtlantaFX selected for the GUI; this US declares both dependencies
- **ADR-010** — TestFX identified for future GUI testing; dependency added here in testImplementation scope
