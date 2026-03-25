# FixKitty Phase 1 — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a functional Linux desktop recovery console (Ubuntu 24) with JavaFX GUI and Lanterna TUI that restarts broken subsystems (audio, Bluetooth, network, GNOME shell) through a Clean Architecture.

**Architecture:** Clean Architecture with strict layer separation: `core/domain` (entities, ports) → `application` (use cases) → `infrastructure` (ProcessBuilder, systemctl, Ubuntu24 catalog) → `interfaces/gui` + `interfaces/tui` (thin adapters). Google Guice wires all layers via `AppModule`. GUI and TUI call the same use cases — no business logic in either.

**Tech Stack:** Java 25, Gradle (Kotlin DSL), Google Guice 7, SLF4J + Logback, JavaFX 23 + AtlantaFX + Ikonli, Lanterna 3.1, JUnit 5, Mockito 5.

**Spec:** `docs/architecture/2026-03-23-phase1-design.md`
**User Stories:** `docs/user-stories/`
**Test Cases:** `docs/tests/`

---

## File Map

### Files to create

```text
build.gradle.kts                                              (modify)
settings.gradle.kts                                           (modify)
src/main/resources/logback.xml                                (create)

src/main/java/br/com/josenaldo/fixkitty/
  bootstrap/
    Main.java
    AppModule.java
    InterfaceSelector.java
  core/
    domain/
      RecoveryAction.java
      StepStatus.java
      ResultStatus.java
      FailurePolicy.java
      ExecutionStep.java
      StepResult.java
      ExecutionResult.java
      EnvironmentProfile.java
    ports/
      CommandRunner.java
      PrivilegeManager.java
      EnvironmentDetector.java
      ActionCatalog.java
  application/
    ExecuteRecoveryUseCase.java
    ListActionsUseCase.java
    CheckEnvironmentUseCase.java
  infrastructure/
    command/
      ProcessBuilderCommandRunner.java
    privilege/
      SudoPrivilegeManager.java
      PkexecPrivilegeManager.java
    detectors/
      Ubuntu24EnvironmentDetector.java
    catalog/
      Ubuntu24ActionCatalog.java
  interfaces/
    gui/
      GuiApp.java
      MainController.java
      LogPanel.java
      ResultPanel.java
      EnvironmentPanel.java
      GnomeShellConfirmationDialog.java
      AppRestarter.java
    tui/
      TuiApp.java
      MainMenuScreen.java
      ExecutionScreen.java
      ResultScreen.java
      EnvironmentScreen.java

src/test/java/br/com/josenaldo/fixkitty/
  core/domain/
    RecoveryActionTest.java
    ExecutionStepTest.java
    ExecutionResultTest.java
    EnvironmentProfileTest.java
  application/
    ExecuteRecoveryUseCaseTest.java
    ListActionsUseCaseTest.java
    CheckEnvironmentUseCaseTest.java
  infrastructure/
    command/
      ProcessBuilderCommandRunnerTest.java
    privilege/
      SudoPrivilegeManagerTest.java
      PkexecPrivilegeManagerTest.java
    catalog/
      Ubuntu24ActionCatalogTest.java
```

### Files to delete

```text
src/main/java/org/example/Main.java
```

---

## Phase 1.0 — Project Setup

### Task 1: Configure Gradle build (US-001)

**Files:**
- Modify: `build.gradle.kts`
- Modify: `settings.gradle.kts`
- Create: `src/main/resources/logback.xml`

- [ ] **Step 1: Update `settings.gradle.kts`**

```kotlin
rootProject.name = "fixkitty"
```

- [ ] **Step 2: Replace `build.gradle.kts` with the full dependency set**

```kotlin
plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "br.com.josenaldo"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "br.com.josenaldo.fixkitty.bootstrap.Main"
}

javafx {
    version = "23"
    modules = listOf("javafx.controls", "javafx.fxml")
}

repositories {
    mavenCentral()
}

dependencies {
    // Dependency Injection
    implementation("com.google.inject:guice:7.0.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.12")

    // GUI
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation("org.kordamp.ikonli:ikonli-javafx:12.3.1")
    implementation("org.kordamp.ikonli:ikonli-fontawesome5-pack:12.3.1")

    // TUI
    implementation("com.googlecode.lanterna:lanterna:3.1.2")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.assertj:assertj-core:3.27.3")

    // Future: TestFX for GUI automation (Phase 2+)
    testImplementation("org.testfx:testfx-junit5:4.0.18")
}

tasks.test {
    useJUnitPlatform()
}

tasks.run.get().jvmArgs = listOf("--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED")
```

- [ ] **Step 3: Create `src/main/resources/logback.xml`**

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss} [%level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${user.home}/.fixkitty/logs/fixkitty.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${user.home}/.fixkitty/logs/fixkitty.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%level] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

- [ ] **Step 4: Delete the placeholder**

```bash
rm src/main/java/org/example/Main.java
rmdir src/main/java/org/example
```

- [ ] **Step 5: Verify the build compiles**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add build.gradle.kts settings.gradle.kts src/main/resources/logback.xml
git commit -m "build: configure Gradle with full Phase 1 dependency stack"
```

---

### Task 2: Create package structure (US-002)

**Files:**
- Create: `package-info.java` in each package

- [ ] **Step 1: Create package-info.java for each package**

Create `src/main/java/br/com/josenaldo/fixkitty/core/domain/package-info.java`:
```java
/**
 * Domain model for FixKitty.
 *
 * <p>Contains entities, value objects, and enums that model the recovery
 * problem. This package has no dependency on any other layer.
 */
package br.com.josenaldo.fixkitty.core.domain;
```

Create `src/main/java/br/com/josenaldo/fixkitty/core/ports/package-info.java`:
```java
/**
 * Port interfaces (abstractions) for the FixKitty core layer.
 *
 * <p>Interfaces defined here are implemented by the infrastructure layer
 * and injected into use cases. This package depends only on domain types.
 */
package br.com.josenaldo.fixkitty.core.ports;
```

Create `src/main/java/br/com/josenaldo/fixkitty/application/package-info.java`:
```java
/**
 * Application use cases for FixKitty.
 *
 * <p>Orchestrates domain objects and ports to implement business workflows.
 * Depends only on {@code core.domain} and {@code core.ports}. Never imports
 * infrastructure or interface classes directly.
 */
package br.com.josenaldo.fixkitty.application;
```

Create `src/main/java/br/com/josenaldo/fixkitty/infrastructure/command/package-info.java`:
```java
/**
 * Infrastructure adapters for OS command execution.
 *
 * <p>Implements {@code CommandRunner} using {@link java.lang.ProcessBuilder}.
 */
package br.com.josenaldo.fixkitty.infrastructure.command;
```

Create `src/main/java/br/com/josenaldo/fixkitty/infrastructure/privilege/package-info.java`:
```java
/**
 * Infrastructure adapters for privilege escalation.
 *
 * <p>Implements {@code PrivilegeManager} using sudo (TUI) and pkexec (GUI).
 */
package br.com.josenaldo.fixkitty.infrastructure.privilege;
```

Create `src/main/java/br/com/josenaldo/fixkitty/infrastructure/detectors/package-info.java`:
```java
/**
 * Infrastructure adapters for Linux environment detection.
 *
 * <p>Implements {@code EnvironmentDetector} for specific Linux distributions.
 */
package br.com.josenaldo.fixkitty.infrastructure.detectors;
```

Create `src/main/java/br/com/josenaldo/fixkitty/infrastructure/catalog/package-info.java`:
```java
/**
 * Infrastructure adapters for recovery action catalogs.
 *
 * <p>Implements {@code ActionCatalog} with distro-specific command definitions.
 */
package br.com.josenaldo.fixkitty.infrastructure.catalog;
```

Create `src/main/java/br/com/josenaldo/fixkitty/interfaces/gui/package-info.java`:
```java
/**
 * JavaFX graphical user interface for FixKitty.
 *
 * <p>Thin adapter layer: receives user input, delegates to use cases,
 * and renders results. Contains no business logic. May import AtlantaFX
 * and Ikonli. Never imported by core, application, or infrastructure.
 */
package br.com.josenaldo.fixkitty.interfaces.gui;
```

Create `src/main/java/br/com/josenaldo/fixkitty/interfaces/tui/package-info.java`:
```java
/**
 * Lanterna terminal user interface for FixKitty.
 *
 * <p>Thin adapter layer for TTY environments. Lanterna classes are confined
 * exclusively to this package and must never be imported elsewhere.
 */
package br.com.josenaldo.fixkitty.interfaces.tui;
```

Create `src/main/java/br/com/josenaldo/fixkitty/bootstrap/package-info.java`:
```java
/**
 * Bootstrap and dependency wiring for FixKitty.
 *
 * <p>Contains {@code Main}, {@code AppModule} (Guice), and {@code InterfaceSelector}.
 * The only package allowed to import from all layers simultaneously.
 */
package br.com.josenaldo.fixkitty.bootstrap;
```

- [ ] **Step 2: Verify build still passes**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/
git commit -m "feat: create Clean Architecture package structure with package-info docs"
```

---

### Task 3: Bootstrap — Main, AppModule, InterfaceSelector (US-003)

**Files:**
- Create: `src/main/java/br/com/josenaldo/fixkitty/bootstrap/Main.java`
- Create: `src/main/java/br/com/josenaldo/fixkitty/bootstrap/AppModule.java`
- Create: `src/main/java/br/com/josenaldo/fixkitty/bootstrap/InterfaceSelector.java`

- [ ] **Step 1: Create `AppModule.java`**

```java
package br.com.josenaldo.fixkitty.bootstrap;

import com.google.inject.AbstractModule;

/**
 * Guice dependency injection module for FixKitty.
 *
 * <p>Binds all interface implementations to their port abstractions.
 * This is the single wiring point for the entire application.
 */
public class AppModule extends AbstractModule {

    /** Whether the application is running in TUI mode. */
    private final boolean tuiMode;

    /**
     * Creates an AppModule for the specified interface mode.
     *
     * @param tuiMode {@code true} to configure TUI bindings; {@code false} for GUI
     */
    public AppModule(boolean tuiMode) {
        this.tuiMode = tuiMode;
    }

    @Override
    protected void configure() {
        // Bindings will be added as infrastructure classes are implemented
    }
}
```

- [ ] **Step 2: Create `InterfaceSelector.java`**

```java
package br.com.josenaldo.fixkitty.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Determines which interface to launch based on runtime context.
 *
 * <p>Selects TUI when the {@code --tui} argument is present or when no
 * graphical session is detected. Falls back to TUI if GUI launch fails.
 */
public class InterfaceSelector {

    private static final Logger log = LoggerFactory.getLogger(InterfaceSelector.class);

    /**
     * Returns {@code true} if the application should run in TUI mode.
     *
     * @param args command-line arguments passed to the application
     * @return {@code true} for TUI, {@code false} for GUI
     */
    public boolean shouldUseTui(String[] args) {
        for (String arg : args) {
            if ("--tui".equals(arg)) {
                log.info("TUI mode selected via --tui argument");
                return true;
            }
        }
        boolean hasDisplay = System.getenv("DISPLAY") != null
                || System.getenv("WAYLAND_DISPLAY") != null;
        if (!hasDisplay) {
            log.info("No graphical session detected, falling back to TUI");
            return true;
        }
        return false;
    }
}
```

- [ ] **Step 3: Create `Main.java`**

```java
package br.com.josenaldo.fixkitty.bootstrap;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application entry point for FixKitty.
 *
 * <p>Creates the Guice injector and delegates to the appropriate interface
 * (GUI or TUI) based on runtime arguments and environment.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * Application entry point.
     *
     * @param args command-line arguments; {@code --tui} selects terminal mode
     */
    public static void main(String[] args) {
        log.info("FixKitty starting...");
        InterfaceSelector selector = new InterfaceSelector();
        boolean tuiMode = selector.shouldUseTui(args);

        Injector injector = Guice.createInjector(new AppModule(tuiMode));

        if (tuiMode) {
            log.info("Launching TUI");
            // TuiApp will be wired here in Phase 1.4
            System.out.println("[FixKitty TUI — not yet implemented]");
        } else {
            log.info("Launching GUI");
            // GuiApp will be launched here in Phase 1.5
            System.out.println("[FixKitty GUI — not yet implemented]");
        }
    }
}
```

- [ ] **Step 4: Verify the build and run**

```bash
./gradlew build
./gradlew run
./gradlew run --args="--tui"
```

Expected: build succeeds; each prints its placeholder message.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat: add bootstrap — Main, AppModule, InterfaceSelector"
```

---

## Phase 1.1 — Core Domain

### Task 4: Domain enums (US-004)

**Files:**
- Create: `src/main/java/br/com/josenaldo/fixkitty/core/domain/RecoveryAction.java`
- Create: `src/main/java/br/com/josenaldo/fixkitty/core/domain/StepStatus.java`
- Create: `src/main/java/br/com/josenaldo/fixkitty/core/domain/ResultStatus.java`
- Create: `src/main/java/br/com/josenaldo/fixkitty/core/domain/FailurePolicy.java`
- Test: `src/test/java/br/com/josenaldo/fixkitty/core/domain/RecoveryActionTest.java`

- [ ] **Step 1: Write failing test for `RecoveryAction`**

Create `src/test/java/br/com/josenaldo/fixkitty/core/domain/RecoveryActionTest.java`:

```java
package br.com.josenaldo.fixkitty.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecoveryActionTest {

    @Test
    void allActionsHaveDisplayName() {
        for (RecoveryAction action : RecoveryAction.values()) {
            assertNotNull(action.displayName(), "displayName must not be null for " + action);
            assertFalse(action.displayName().isBlank(), "displayName must not be blank for " + action);
        }
    }

    @Test
    void allSixActionsExist() {
        assertEquals(6, RecoveryAction.values().length);
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.core.domain.RecoveryActionTest"
```

Expected: FAIL — class not found.

- [ ] **Step 3: Create `RecoveryAction.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

/**
 * Enumerates all recovery actions supported by FixKitty.
 *
 * <p>Each constant represents a subsystem recovery operation. The
 * {@link #CHECK_ENVIRONMENT} action is included for unified menu routing
 * and produces an empty execution plan.
 */
public enum RecoveryAction {

    /** Restarts the PipeWire audio server and WirePlumber session manager. */
    FIX_AUDIO("Fix Audio"),

    /** Restarts the system Bluetooth service. */
    FIX_BLUETOOTH("Fix Bluetooth"),

    /** Restarts the NetworkManager service. */
    FIX_NETWORK("Fix Network"),

    /** Restarts the GNOME shell process. */
    FIX_GNOME_SHELL("Fix GNOME Shell"),

    /** Executes all fix actions in sequence. */
    FIX_ALL("Fix All"),

    /** Detects and displays the current environment profile. */
    CHECK_ENVIRONMENT("Check Environment");

    private final String displayName;

    RecoveryAction(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable label for this action.
     *
     * @return the display name, never {@code null} or blank
     */
    public String displayName() {
        return displayName;
    }
}
```

- [ ] **Step 4: Create `StepStatus.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

/**
 * Outcome status for a single execution step.
 */
public enum StepStatus {
    /** Command completed with exit code 0. */
    SUCCESS,
    /** Command completed with a non-zero exit code. */
    FAILED,
    /** Step was not executed due to a prior ABORT failure policy. */
    SKIPPED,
    /** Command did not complete within the allowed timeout. */
    TIMEOUT
}
```

- [ ] **Step 5: Create `ResultStatus.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

/**
 * Overall outcome status for a completed recovery action.
 *
 * <p>Aggregated from the individual {@link StepStatus} results according
 * to the rules defined in the Phase 1 design spec.
 */
public enum ResultStatus {
    /** All steps completed successfully. */
    SUCCESS,
    /** At least one step succeeded and at least one failed (with CONTINUE policy). */
    PARTIAL,
    /** All non-skipped steps failed, or the first step failed with ABORT policy. */
    FAILED
}
```

- [ ] **Step 6: Create `FailurePolicy.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

/**
 * Defines how execution should proceed when a step fails.
 */
public enum FailurePolicy {
    /**
     * Stop execution immediately. Mark all remaining steps as
     * {@link StepStatus#SKIPPED}.
     */
    ABORT,

    /**
     * Continue executing remaining steps regardless of this step's failure.
     * The overall result may be {@link ResultStatus#PARTIAL} or
     * {@link ResultStatus#FAILED}.
     */
    CONTINUE,

    /**
     * Continue executing remaining steps. Treat this step's failure as
     * non-critical: it does not lower the overall result to
     * {@link ResultStatus#FAILED} if at least one step succeeded.
     */
    WARN
}
```

- [ ] **Step 7: Run test to confirm it passes**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.core.domain.RecoveryActionTest"
```

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat(domain): add RecoveryAction, StepStatus, ResultStatus, FailurePolicy enums"
```

---

### Task 5: Domain entities (US-004 continued)

**Files:**
- Create: `ExecutionStep.java`, `StepResult.java`, `ExecutionResult.java`, `EnvironmentProfile.java`
- Test: `ExecutionStepTest.java`, `ExecutionResultTest.java`, `EnvironmentProfileTest.java`

- [ ] **Step 1: Write failing tests**

Create `src/test/java/br/com/josenaldo/fixkitty/core/domain/ExecutionStepTest.java`:

```java
package br.com.josenaldo.fixkitty.core.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExecutionStepTest {

    @Test
    void constructor_setsAllFields() {
        String[] cmd = {"echo", "hello"};
        ExecutionStep step = new ExecutionStep("id1", "Echo hello", cmd, false, 5, FailurePolicy.CONTINUE);

        assertEquals("id1", step.id());
        assertEquals("Echo hello", step.description());
        assertArrayEquals(cmd, step.command());
        assertFalse(step.requiresPrivilege());
        assertEquals(5, step.timeoutSeconds());
        assertEquals(FailurePolicy.CONTINUE, step.onFailure());
    }

    @Test
    void constructor_withPrivilegeRequired() {
        ExecutionStep step = new ExecutionStep("s1", "desc", new String[]{"sudo", "cmd"}, true, 10, FailurePolicy.ABORT);
        assertTrue(step.requiresPrivilege());
    }
}
```

Create `src/test/java/br/com/josenaldo/fixkitty/core/domain/ExecutionResultTest.java`:

```java
package br.com.josenaldo.fixkitty.core.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExecutionResultTest {

    @Test
    void isSuccess_whenStatusSuccess_returnsTrue() {
        ExecutionResult result = new ExecutionResult(
            RecoveryAction.FIX_AUDIO, ResultStatus.SUCCESS, List.of(),
            Instant.now(), Instant.now(), null);
        assertTrue(result.isSuccess());
    }

    @Test
    void isSuccess_whenStatusFailed_returnsFalse() {
        ExecutionResult result = new ExecutionResult(
            RecoveryAction.FIX_AUDIO, ResultStatus.FAILED, List.of(),
            Instant.now(), Instant.now(), "Try again.");
        assertFalse(result.isSuccess());
    }

    @Test
    void recommendation_canBeNull() {
        ExecutionResult result = new ExecutionResult(
            RecoveryAction.FIX_AUDIO, ResultStatus.SUCCESS, List.of(),
            Instant.now(), Instant.now(), null);
        assertNull(result.recommendation());
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.core.domain.*"
```

Expected: FAIL — classes not found.

- [ ] **Step 3: Create `ExecutionStep.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

/**
 * Represents a single command step within a recovery action's execution plan.
 *
 * <p>Steps are immutable. The {@code command} array contains the OS command
 * to execute. If {@code requiresPrivilege} is {@code true}, the command array
 * must be escalated by the {@code PrivilegeManager} before execution.
 *
 * @param id                unique step identifier within its action
 * @param description       human-readable description shown to the user
 * @param command           the OS command to execute as an array of arguments
 * @param requiresPrivilege whether privilege escalation (sudo/pkexec) is needed
 * @param timeoutSeconds    maximum allowed execution time in seconds
 * @param onFailure         behaviour policy when this step fails
 */
public record ExecutionStep(
        String id,
        String description,
        String[] command,
        boolean requiresPrivilege,
        int timeoutSeconds,
        FailurePolicy onFailure) {
}
```

- [ ] **Step 4: Create `StepResult.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

/**
 * The structured result of executing a single {@link ExecutionStep}.
 *
 * <p>Captures all observable output from the OS process. Never throws
 * exceptions for execution failures — all outcomes are represented as data.
 *
 * @param step           the step that was executed
 * @param status         outcome classification
 * @param exitCode       process exit code; {@code -1} if the process never started
 * @param stdout         standard output captured from the process
 * @param stderr         standard error captured from the process
 * @param durationMs     elapsed execution time in milliseconds
 * @param friendlyMessage human-readable explanation of the outcome
 */
public record StepResult(
        ExecutionStep step,
        StepStatus status,
        int exitCode,
        String stdout,
        String stderr,
        long durationMs,
        String friendlyMessage) {
}
```

- [ ] **Step 5: Create `ExecutionResult.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

import java.time.Instant;
import java.util.List;

/**
 * The aggregated result of executing a {@link RecoveryAction}.
 *
 * <p>Contains the ordered list of step results and an overall status
 * computed from them. The {@code recommendation} field is {@code null}
 * when all steps succeeded; it contains a human-readable next-step
 * suggestion otherwise.
 *
 * @param action         the action that was executed
 * @param status         overall outcome (see ResultStatus aggregation rules in design spec)
 * @param steps          individual step outcomes in execution order
 * @param startedAt      timestamp when execution began
 * @param finishedAt     timestamp when execution completed
 * @param recommendation suggested next step for the user; {@code null} on full success
 */
public record ExecutionResult(
        RecoveryAction action,
        ResultStatus status,
        List<StepResult> steps,
        Instant startedAt,
        Instant finishedAt,
        String recommendation) {

    /**
     * Returns {@code true} if the overall result is {@link ResultStatus#SUCCESS}.
     *
     * @return {@code true} on full success
     */
    public boolean isSuccess() {
        return status == ResultStatus.SUCCESS;
    }
}
```

- [ ] **Step 6: Create `EnvironmentProfile.java`**

```java
package br.com.josenaldo.fixkitty.core.domain;

/**
 * Describes the detected Linux desktop environment.
 *
 * <p>Used by {@code ActionCatalog} to select the correct set of commands
 * for the current system. Immutable value object.
 *
 * @param distro              the Linux distribution name and version (e.g. "Ubuntu 24.04")
 * @param desktop             the desktop environment name (e.g. "GNOME")
 * @param audioStack          the detected audio subsystem (e.g. "PipeWire")
 * @param hasGraphicalSession {@code true} if a graphical display session is active
 */
public record EnvironmentProfile(
        String distro,
        String desktop,
        String audioStack,
        boolean hasGraphicalSession) {
}
```

- [ ] **Step 7: Run all domain tests**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.core.domain.*"
```

Expected: all PASS.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat(domain): add ExecutionStep, StepResult, ExecutionResult, EnvironmentProfile records"
```

---

### Task 6: Port interfaces (US-005)

**Files:**
- Create: `CommandRunner.java`, `PrivilegeManager.java`, `EnvironmentDetector.java`, `ActionCatalog.java`

No tests needed — interfaces have no behavior to test.

- [ ] **Step 1: Create `CommandRunner.java`**

```java
package br.com.josenaldo.fixkitty.core.ports;

import br.com.josenaldo.fixkitty.core.domain.ExecutionStep;
import br.com.josenaldo.fixkitty.core.domain.StepResult;

/**
 * Port for executing OS commands.
 *
 * <p>Implementations must capture stdout and stderr, enforce the step's
 * timeout, and return a structured {@link StepResult}. Must never throw
 * exceptions for execution failures — all failure states are represented
 * in the returned {@code StepResult}.
 */
public interface CommandRunner {

    /**
     * Executes the command defined in the given step.
     *
     * <p>The command array in {@code step} is already escalated if privilege
     * was required; this method always executes exactly what is in the array.
     *
     * @param step the step to execute
     * @return a fully populated result; never {@code null}
     */
    StepResult execute(ExecutionStep step);
}
```

- [ ] **Step 2: Create `PrivilegeManager.java`**

```java
package br.com.josenaldo.fixkitty.core.ports;

/**
 * Port for privilege escalation of OS commands.
 *
 * <p>Transforms a plain command array into one that includes the
 * escalation mechanism (e.g. sudo, pkexec). The exact mechanism
 * depends on the active interface (TUI vs GUI).
 */
public interface PrivilegeManager {

    /**
     * Wraps the given command array with privilege escalation.
     *
     * @param command the original command array to escalate
     * @return a new array with the escalation prefix prepended
     */
    String[] escalate(String[] command);
}
```

- [ ] **Step 3: Create `EnvironmentDetector.java`**

```java
package br.com.josenaldo.fixkitty.core.ports;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;

/**
 * Port for detecting the current Linux desktop environment.
 *
 * <p>Implementations read OS-specific signals (environment variables,
 * config files, running services) to populate an {@link EnvironmentProfile}.
 */
public interface EnvironmentDetector {

    /**
     * Detects and returns a description of the current environment.
     *
     * @return the detected profile; never {@code null}
     */
    EnvironmentProfile detect();
}
```

- [ ] **Step 4: Create `ActionCatalog.java`**

```java
package br.com.josenaldo.fixkitty.core.ports;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.domain.ExecutionStep;
import br.com.josenaldo.fixkitty.core.domain.RecoveryAction;
import java.util.List;

/**
 * Port for retrieving recovery action definitions for a given environment.
 *
 * <p>Decouples the business logic from the specific commands used on each
 * Linux distribution. Implementations supply the concrete command arrays,
 * privilege requirements, and default recommendations.
 */
public interface ActionCatalog {

    /**
     * Returns the recovery actions available for the given environment.
     *
     * @param profile the detected environment profile
     * @return an ordered list of available actions; never {@code null}
     */
    List<RecoveryAction> actionsFor(EnvironmentProfile profile);

    /**
     * Returns the ordered list of execution steps for the given action.
     *
     * <p>For {@link RecoveryAction#CHECK_ENVIRONMENT}, returns an empty list.
     *
     * @param action  the recovery action to look up
     * @param profile the environment profile to tailor steps for
     * @return the execution steps; never {@code null}, may be empty
     */
    List<ExecutionStep> planFor(RecoveryAction action, EnvironmentProfile profile);

    /**
     * Returns a human-readable recommendation to display on partial or failed results.
     *
     * @param action the action that produced a non-success result
     * @return the recommendation text, or {@code null} if not applicable
     */
    String defaultRecommendationFor(RecoveryAction action);
}
```

- [ ] **Step 5: Verify build passes**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat(core): add port interfaces — CommandRunner, PrivilegeManager, EnvironmentDetector, ActionCatalog"
```

---

## Phase 1.2 — Application Use Cases

### Task 7: CheckEnvironmentUseCase and ListActionsUseCase (US-007, US-008)

**Files:**
- Create: `CheckEnvironmentUseCase.java`, `ListActionsUseCase.java`
- Test: `CheckEnvironmentUseCaseTest.java`, `ListActionsUseCaseTest.java`

- [ ] **Step 1: Write failing tests**

Create `src/test/java/br/com/josenaldo/fixkitty/application/CheckEnvironmentUseCaseTest.java`:

```java
package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckEnvironmentUseCaseTest {

    @Mock
    EnvironmentDetector detector;

    @InjectMocks
    CheckEnvironmentUseCase useCase;

    @Test
    void execute_delegatesToDetector_returnsProfile() {
        EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);
        when(detector.detect()).thenReturn(profile);

        EnvironmentProfile result = useCase.execute();

        assertSame(profile, result);
        verify(detector).detect();
    }
}
```

Create `src/test/java/br/com/josenaldo/fixkitty/application/ListActionsUseCaseTest.java`:

```java
package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.domain.RecoveryAction;
import br.com.josenaldo.fixkitty.core.ports.ActionCatalog;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListActionsUseCaseTest {

    @Mock EnvironmentDetector detector;
    @Mock ActionCatalog catalog;

    @InjectMocks
    ListActionsUseCase useCase;

    @Test
    void execute_detectsEnvironmentAndDelegatesToCatalog() {
        EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);
        List<RecoveryAction> actions = List.of(RecoveryAction.FIX_AUDIO, RecoveryAction.FIX_BLUETOOTH);
        when(detector.detect()).thenReturn(profile);
        when(catalog.actionsFor(profile)).thenReturn(actions);

        List<RecoveryAction> result = useCase.execute();

        assertEquals(actions, result);
        verify(detector).detect();
        verify(catalog).actionsFor(profile);
    }
}
```

- [ ] **Step 2: Run to confirm failure**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.application.*"
```

Expected: FAIL — classes not found.

- [ ] **Step 3: Create `CheckEnvironmentUseCase.java`**

```java
package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import com.google.inject.Inject;

/**
 * Use case that detects and returns the current Linux environment profile.
 *
 * <p>Delegates entirely to the injected {@link EnvironmentDetector}.
 * Contains no business logic beyond delegation.
 */
public class CheckEnvironmentUseCase {

    private final EnvironmentDetector detector;

    /**
     * Creates a new use case with the given environment detector.
     *
     * @param detector the detector to use for environment discovery
     */
    @Inject
    public CheckEnvironmentUseCase(EnvironmentDetector detector) {
        this.detector = detector;
    }

    /**
     * Detects and returns the current environment profile.
     *
     * @return the detected profile; never {@code null}
     */
    public EnvironmentProfile execute() {
        return detector.detect();
    }
}
```

- [ ] **Step 4: Create `ListActionsUseCase.java`**

```java
package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.RecoveryAction;
import br.com.josenaldo.fixkitty.core.ports.ActionCatalog;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import com.google.inject.Inject;
import java.util.List;

/**
 * Use case that returns the recovery actions available in the current environment.
 *
 * <p>Detects the environment first, then queries the action catalog for
 * the matching set of supported actions.
 */
public class ListActionsUseCase {

    private final EnvironmentDetector detector;
    private final ActionCatalog catalog;

    /**
     * Creates a new use case with the required dependencies.
     *
     * @param detector the detector used to identify the current environment
     * @param catalog  the catalog used to look up available actions
     */
    @Inject
    public ListActionsUseCase(EnvironmentDetector detector, ActionCatalog catalog) {
        this.detector = detector;
        this.catalog = catalog;
    }

    /**
     * Returns the list of recovery actions supported in the current environment.
     *
     * @return ordered list of available actions; never {@code null}
     */
    public List<RecoveryAction> execute() {
        return catalog.actionsFor(detector.detect());
    }
}
```

- [ ] **Step 5: Run tests — confirm PASS**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.application.*"
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat(application): add CheckEnvironmentUseCase and ListActionsUseCase"
```

---

### Task 8: ExecuteRecoveryUseCase (US-006)

**Files:**
- Create: `src/main/java/br/com/josenaldo/fixkitty/application/ExecuteRecoveryUseCase.java`
- Test: `src/test/java/br/com/josenaldo/fixkitty/application/ExecuteRecoveryUseCaseTest.java`

- [ ] **Step 1: Write failing tests**

Create `src/test/java/br/com/josenaldo/fixkitty/application/ExecuteRecoveryUseCaseTest.java`:

```java
package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecuteRecoveryUseCaseTest {

    @Mock EnvironmentDetector detector;
    @Mock ActionCatalog catalog;
    @Mock CommandRunner runner;
    @Mock PrivilegeManager privilege;

    ExecuteRecoveryUseCase useCase;

    EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);

    ExecutionStep stepNoPrivilege = new ExecutionStep("s1", "Echo", new String[]{"echo","hi"}, false, 5, FailurePolicy.CONTINUE);
    ExecutionStep stepWithPrivilege = new ExecutionStep("s2", "Sudo cmd", new String[]{"cmd"}, true, 5, FailurePolicy.ABORT);
    ExecutionStep stepAbort = new ExecutionStep("s3", "Abort step", new String[]{"fail"}, false, 5, FailurePolicy.ABORT);
    ExecutionStep stepContinue = new ExecutionStep("s4", "Continue step", new String[]{"ok"}, false, 5, FailurePolicy.CONTINUE);

    StepResult success(ExecutionStep s) {
        return new StepResult(s, StepStatus.SUCCESS, 0, "ok", "", 10, "Completed successfully");
    }

    StepResult failed(ExecutionStep s) {
        return new StepResult(s, StepStatus.FAILED, 1, "", "error", 10, "Command exited with code 1");
    }

    @BeforeEach
    void setup() {
        useCase = new ExecuteRecoveryUseCase(detector, catalog, runner, privilege);
        when(detector.detect()).thenReturn(profile);
    }

    @Test
    void execute_allStepsSucceed_returnsSuccess() {
        when(catalog.planFor(RecoveryAction.FIX_AUDIO, profile))
            .thenReturn(List.of(stepNoPrivilege));
        when(runner.execute(stepNoPrivilege)).thenReturn(success(stepNoPrivilege));

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_AUDIO);

        assertEquals(ResultStatus.SUCCESS, result.status());
        assertNull(result.recommendation());
        assertTrue(result.isSuccess());
    }

    @Test
    void execute_oneStepFails_withContinue_returnsPartial() {
        ExecutionStep step2 = new ExecutionStep("s2b", "Step 2", new String[]{"ok"}, false, 5, FailurePolicy.CONTINUE);
        when(catalog.planFor(RecoveryAction.FIX_AUDIO, profile))
            .thenReturn(List.of(stepContinue, step2));
        when(runner.execute(stepContinue)).thenReturn(failed(stepContinue));
        when(runner.execute(step2)).thenReturn(success(step2));
        when(catalog.defaultRecommendationFor(RecoveryAction.FIX_AUDIO)).thenReturn("Try again.");

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_AUDIO);

        assertEquals(ResultStatus.PARTIAL, result.status());
        assertEquals(2, result.steps().size());
        assertEquals("Try again.", result.recommendation());
    }

    @Test
    void execute_firstStepFails_withAbort_remainingAreSkipped() {
        ExecutionStep step2 = new ExecutionStep("s2c", "After abort", new String[]{"ok"}, false, 5, FailurePolicy.CONTINUE);
        when(catalog.planFor(RecoveryAction.FIX_BLUETOOTH, profile))
            .thenReturn(List.of(stepAbort, step2));
        when(runner.execute(stepAbort)).thenReturn(failed(stepAbort));
        when(catalog.defaultRecommendationFor(RecoveryAction.FIX_BLUETOOTH)).thenReturn("Check service.");

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_BLUETOOTH);

        assertEquals(ResultStatus.FAILED, result.status());
        assertEquals(2, result.steps().size());
        assertEquals(StepStatus.FAILED, result.steps().get(0).status());
        assertEquals(StepStatus.SKIPPED, result.steps().get(1).status());
        verify(runner, never()).execute(step2);
    }

    @Test
    void execute_privilegedStep_callsPrivilegeManager() {
        String[] escalated = {"sudo", "cmd"};
        when(privilege.escalate(stepWithPrivilege.command())).thenReturn(escalated);
        when(catalog.planFor(RecoveryAction.FIX_BLUETOOTH, profile))
            .thenReturn(List.of(stepWithPrivilege));
        // The runner receives a step with the escalated command
        when(runner.execute(any(ExecutionStep.class))).thenReturn(
            new StepResult(stepWithPrivilege, StepStatus.SUCCESS, 0, "", "", 10, "OK"));

        useCase.execute(RecoveryAction.FIX_BLUETOOTH);

        verify(privilege).escalate(stepWithPrivilege.command());
    }

    @Test
    void execute_fullSuccess_recommendationIsNull() {
        when(catalog.planFor(RecoveryAction.FIX_NETWORK, profile))
            .thenReturn(List.of(stepNoPrivilege));
        when(runner.execute(stepNoPrivilege)).thenReturn(success(stepNoPrivilege));

        ExecutionResult result = useCase.execute(RecoveryAction.FIX_NETWORK);

        assertNull(result.recommendation());
        verify(catalog, never()).defaultRecommendationFor(any());
    }
}
```

- [ ] **Step 2: Run tests to confirm failure**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.application.ExecuteRecoveryUseCaseTest"
```

Expected: FAIL — class not found.

- [ ] **Step 3: Create `ExecuteRecoveryUseCase.java`**

```java
package br.com.josenaldo.fixkitty.application;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.*;
import com.google.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case that executes a recovery action and returns a structured result.
 *
 * <p>Orchestrates environment detection, step planning, command execution,
 * privilege escalation, and result aggregation. Never throws exceptions for
 * execution failures — all failure states are captured in {@link ExecutionResult}.
 */
public class ExecuteRecoveryUseCase {

    private final EnvironmentDetector detector;
    private final ActionCatalog catalog;
    private final CommandRunner runner;
    private final PrivilegeManager privilege;

    /**
     * Creates the use case with all required ports.
     *
     * @param detector  detects the current Linux environment
     * @param catalog   supplies the execution plan for each action
     * @param runner    executes OS commands and captures output
     * @param privilege escalates commands that require elevated privileges
     */
    @Inject
    public ExecuteRecoveryUseCase(EnvironmentDetector detector,
                                   ActionCatalog catalog,
                                   CommandRunner runner,
                                   PrivilegeManager privilege) {
        this.detector = detector;
        this.catalog = catalog;
        this.runner = runner;
        this.privilege = privilege;
    }

    /**
     * Executes the given recovery action and returns the aggregated result.
     *
     * @param action the action to execute
     * @return the fully populated execution result; never {@code null}
     */
    public ExecutionResult execute(RecoveryAction action) {
        Instant startedAt = Instant.now();
        EnvironmentProfile profile = detector.detect();
        List<ExecutionStep> plan = catalog.planFor(action, profile);
        List<StepResult> results = new ArrayList<>();
        boolean aborted = false;

        for (ExecutionStep step : plan) {
            if (aborted) {
                results.add(skipped(step));
                continue;
            }
            ExecutionStep toRun = step.requiresPrivilege()
                    ? escalated(step)
                    : step;
            StepResult stepResult = runner.execute(toRun);
            results.add(stepResult);

            boolean failed = stepResult.status() == StepStatus.FAILED
                    || stepResult.status() == StepStatus.TIMEOUT;
            if (failed && step.onFailure() == FailurePolicy.ABORT) {
                aborted = true;
            }
        }

        ResultStatus overallStatus = aggregate(results);
        String recommendation = overallStatus == ResultStatus.SUCCESS
                ? null
                : catalog.defaultRecommendationFor(action);

        return new ExecutionResult(action, overallStatus, results, startedAt, Instant.now(), recommendation);
    }

    private ExecutionStep escalated(ExecutionStep step) {
        String[] escalatedCommand = privilege.escalate(step.command());
        return new ExecutionStep(step.id(), step.description(), escalatedCommand,
                false, step.timeoutSeconds(), step.onFailure());
    }

    private StepResult skipped(ExecutionStep step) {
        return new StepResult(step, StepStatus.SKIPPED, -1, "", "", 0, "Skipped due to prior failure");
    }

    private ResultStatus aggregate(List<StepResult> results) {
        long successCount = results.stream()
                .filter(r -> r.status() == StepStatus.SUCCESS).count();
        long failedCount = results.stream()
                .filter(r -> r.status() == StepStatus.FAILED || r.status() == StepStatus.TIMEOUT).count();
        long warnFailedCount = results.stream()
                .filter(r -> (r.status() == StepStatus.FAILED || r.status() == StepStatus.TIMEOUT)
                        && r.step().onFailure() == FailurePolicy.WARN).count();

        if (failedCount == 0) return ResultStatus.SUCCESS;
        if (successCount == 0) return ResultStatus.FAILED;
        if (warnFailedCount == failedCount) return ResultStatus.SUCCESS; // all failures were WARN
        return ResultStatus.PARTIAL;
    }
}
```

- [ ] **Step 4: Run tests — confirm PASS**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.application.ExecuteRecoveryUseCaseTest"
```

Expected: all PASS.

- [ ] **Step 5: Run all tests**

```bash
./gradlew test
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat(application): add ExecuteRecoveryUseCase with FailurePolicy and result aggregation"
```

---

## Phase 1.3 — Infrastructure

### Task 9: SudoPrivilegeManager and PkexecPrivilegeManager (US-010, US-011)

**Files:**
- Create: `SudoPrivilegeManager.java`, `PkexecPrivilegeManager.java`
- Test: `SudoPrivilegeManagerTest.java`, `PkexecPrivilegeManagerTest.java`

- [ ] **Step 1: Write failing tests**

Create `src/test/java/br/com/josenaldo/fixkitty/infrastructure/privilege/SudoPrivilegeManagerTest.java`:

```java
package br.com.josenaldo.fixkitty.infrastructure.privilege;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SudoPrivilegeManagerTest {

    SudoPrivilegeManager manager = new SudoPrivilegeManager();

    @Test
    void escalate_prependsSudo() {
        String[] result = manager.escalate(new String[]{"systemctl", "restart", "bluetooth"});
        assertThat(result).containsExactly("sudo", "systemctl", "restart", "bluetooth");
    }

    @Test
    void escalate_doesNotMutateInput() {
        String[] input = {"cmd"};
        manager.escalate(input);
        assertThat(input).containsExactly("cmd");
    }
}
```

Create `src/test/java/br/com/josenaldo/fixkitty/infrastructure/privilege/PkexecPrivilegeManagerTest.java`:

```java
package br.com.josenaldo.fixkitty.infrastructure.privilege;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PkexecPrivilegeManagerTest {

    PkexecPrivilegeManager manager = new PkexecPrivilegeManager();

    @Test
    void escalate_prependsPkexec() {
        String[] result = manager.escalate(new String[]{"systemctl", "restart", "NetworkManager"});
        assertThat(result).containsExactly("pkexec", "systemctl", "restart", "NetworkManager");
    }

    @Test
    void escalate_doesNotMutateInput() {
        String[] input = {"cmd"};
        manager.escalate(input);
        assertThat(input).containsExactly("cmd");
    }
}
```

- [ ] **Step 2: Run tests to confirm failure**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.infrastructure.privilege.*"
```

Expected: FAIL.

- [ ] **Step 3: Create `SudoPrivilegeManager.java`**

```java
package br.com.josenaldo.fixkitty.infrastructure.privilege;

import br.com.josenaldo.fixkitty.core.ports.PrivilegeManager;

/**
 * Privilege manager that prepends {@code sudo} to the command array.
 *
 * <p>Used in the TUI, where the password is entered inline in the terminal.
 */
public class SudoPrivilegeManager implements PrivilegeManager {

    /**
     * Prepends {@code "sudo"} to the given command array.
     *
     * @param command the original command array; not modified
     * @return a new array with {@code "sudo"} as the first element
     */
    @Override
    public String[] escalate(String[] command) {
        String[] escalated = new String[command.length + 1];
        escalated[0] = "sudo";
        System.arraycopy(command, 0, escalated, 1, command.length);
        return escalated;
    }
}
```

- [ ] **Step 4: Create `PkexecPrivilegeManager.java`**

```java
package br.com.josenaldo.fixkitty.infrastructure.privilege;

import br.com.josenaldo.fixkitty.core.ports.PrivilegeManager;

/**
 * Privilege manager that wraps commands with {@code pkexec} (polkit).
 *
 * <p>Used in the GUI to display a native graphical authentication dialog.
 */
public class PkexecPrivilegeManager implements PrivilegeManager {

    /**
     * Prepends {@code "pkexec"} to the given command array.
     *
     * @param command the original command array; not modified
     * @return a new array with {@code "pkexec"} as the first element
     */
    @Override
    public String[] escalate(String[] command) {
        String[] escalated = new String[command.length + 1];
        escalated[0] = "pkexec";
        System.arraycopy(command, 0, escalated, 1, command.length);
        return escalated;
    }
}
```

- [ ] **Step 5: Run tests — confirm PASS**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.infrastructure.privilege.*"
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat(infra): add SudoPrivilegeManager and PkexecPrivilegeManager"
```

---

### Task 10: ProcessBuilderCommandRunner (US-009)

**Files:**
- Create: `src/main/java/br/com/josenaldo/fixkitty/infrastructure/command/ProcessBuilderCommandRunner.java`
- Test: `src/test/java/br/com/josenaldo/fixkitty/infrastructure/command/ProcessBuilderCommandRunnerTest.java`

- [ ] **Step 1: Write integration tests (real commands — no mocking)**

```java
package br.com.josenaldo.fixkitty.infrastructure.command;

import br.com.josenaldo.fixkitty.core.domain.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessBuilderCommandRunnerTest {

    ProcessBuilderCommandRunner runner = new ProcessBuilderCommandRunner();

    ExecutionStep echoStep = new ExecutionStep(
        "echo", "Echo hello", new String[]{"echo", "hello"},
        false, 5, FailurePolicy.CONTINUE);

    ExecutionStep falseStep = new ExecutionStep(
        "false", "Always fails", new String[]{"false"},
        false, 5, FailurePolicy.CONTINUE);

    ExecutionStep sleepStep = new ExecutionStep(
        "sleep", "Sleep forever", new String[]{"sleep", "60"},
        false, 1, FailurePolicy.CONTINUE);

    @Test
    void execute_successfulCommand_returnsSuccess() {
        StepResult result = runner.execute(echoStep);

        assertEquals(StepStatus.SUCCESS, result.status());
        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("hello"));
        assertTrue(result.durationMs() >= 0);
    }

    @Test
    void execute_failingCommand_returnsFailed() {
        StepResult result = runner.execute(falseStep);

        assertEquals(StepStatus.FAILED, result.status());
        assertNotEquals(0, result.exitCode());
    }

    @Test
    void execute_timeoutCommand_returnsTimeout() {
        StepResult result = runner.execute(sleepStep);

        assertEquals(StepStatus.TIMEOUT, result.status());
        assertEquals(-1, result.exitCode());
    }

    @Test
    void execute_capturesStderr() {
        ExecutionStep step = new ExecutionStep(
            "ls-missing", "List missing dir", new String[]{"ls", "/nonexistent_dir_xyz"},
            false, 5, FailurePolicy.CONTINUE);

        StepResult result = runner.execute(step);

        assertEquals(StepStatus.FAILED, result.status());
        assertFalse(result.stderr().isBlank());
    }
}
```

- [ ] **Step 2: Run tests to confirm failure**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.infrastructure.command.ProcessBuilderCommandRunnerTest"
```

Expected: FAIL — class not found.

- [ ] **Step 3: Create `ProcessBuilderCommandRunner.java`**

```java
package br.com.josenaldo.fixkitty.infrastructure.command;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.CommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.concurrent.*;

/**
 * Executes OS commands using {@link ProcessBuilder}.
 *
 * <p>Captures stdout and stderr concurrently to avoid deadlocks.
 * Enforces the step's timeout and terminates the process forcibly on overflow.
 * Never throws for execution failures — all outcomes are captured in
 * the returned {@link StepResult}.
 */
public class ProcessBuilderCommandRunner implements CommandRunner {

    private static final Logger log = LoggerFactory.getLogger(ProcessBuilderCommandRunner.class);

    /**
     * Executes the command in the given step and returns a structured result.
     *
     * @param step the step to execute; the command array is used as-is
     * @return a fully populated result; never {@code null}
     */
    @Override
    public StepResult execute(ExecutionStep step) {
        long startMs = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(step.command());
            pb.redirectErrorStream(false);
            Process process = pb.start();

            // Drain streams concurrently to prevent deadlock
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            Future<String> stdoutFuture = executor.submit(() -> drain(process.getInputStream()));
            Future<String> stderrFuture = executor.submit(() -> drain(process.getErrorStream()));

            boolean finished = process.waitFor(step.timeoutSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                executor.shutdownNow();
                long duration = System.currentTimeMillis() - startMs;
                log.warn("Command timed out after {}s: {}", step.timeoutSeconds(), step.id());
                return new StepResult(step, StepStatus.TIMEOUT, -1, "", "",
                        duration, "Command timed out after " + step.timeoutSeconds() + " seconds");
            }

            String stdout = stdoutFuture.get();
            String stderr = stderrFuture.get();
            executor.shutdown();

            int exitCode = process.exitValue();
            long duration = System.currentTimeMillis() - startMs;
            StepStatus status = exitCode == 0 ? StepStatus.SUCCESS : StepStatus.FAILED;
            String message = exitCode == 0
                    ? "Completed successfully"
                    : "Command exited with code " + exitCode;

            log.debug("Command {} finished with exit code {} in {}ms", step.id(), exitCode, duration);
            return new StepResult(step, status, exitCode, stdout, stderr, duration, message);

        } catch (IOException e) {
            long duration = System.currentTimeMillis() - startMs;
            log.error("Failed to start command {}: {}", step.id(), e.getMessage());
            return new StepResult(step, StepStatus.FAILED, -1, "", e.getMessage(),
                    duration, "Failed to start command: " + e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            long duration = System.currentTimeMillis() - startMs;
            return new StepResult(step, StepStatus.FAILED, -1, "", e.getMessage(),
                    duration, "Execution interrupted");
        }
    }

    private String drain(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            log.warn("Error reading process stream: {}", e.getMessage());
        }
        return sb.toString().stripTrailing();
    }
}
```

- [ ] **Step 4: Run tests — confirm PASS**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.infrastructure.command.ProcessBuilderCommandRunnerTest"
```

Expected: all PASS.

- [ ] **Step 5: Run all tests**

```bash
./gradlew test
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat(infra): add ProcessBuilderCommandRunner with timeout and concurrent stream draining"
```

---

### Task 11: Ubuntu24EnvironmentDetector (US-012)

**Files:**
- Create: `src/main/java/br/com/josenaldo/fixkitty/infrastructure/detectors/Ubuntu24EnvironmentDetector.java`

No unit tests (reads real OS files); verified via manual test and integration.

- [ ] **Step 1: Create `Ubuntu24EnvironmentDetector.java`**

```java
package br.com.josenaldo.fixkitty.infrastructure.detectors;

import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import br.com.josenaldo.fixkitty.core.ports.EnvironmentDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.*;

/**
 * Detects the Linux environment for Ubuntu 24 systems.
 *
 * <p>Reads {@code /etc/os-release} for distro info, uses environment
 * variables for desktop and session detection, and queries systemd for
 * the audio stack.
 */
public class Ubuntu24EnvironmentDetector implements EnvironmentDetector {

    private static final Logger log = LoggerFactory.getLogger(Ubuntu24EnvironmentDetector.class);
    private static final String OS_RELEASE_PATH = "/etc/os-release";

    /**
     * Detects and returns the current environment profile.
     *
     * @return the detected profile with best-effort values; never {@code null}
     */
    @Override
    public EnvironmentProfile detect() {
        String distro = detectDistro();
        String desktop = detectDesktop();
        String audioStack = detectAudioStack();
        boolean hasGraphicalSession = detectGraphicalSession();

        log.info("Environment detected: distro={}, desktop={}, audio={}, graphical={}",
                distro, desktop, audioStack, hasGraphicalSession);

        return new EnvironmentProfile(distro, desktop, audioStack, hasGraphicalSession);
    }

    private String detectDistro() {
        try {
            for (String line : Files.readAllLines(Path.of(OS_RELEASE_PATH))) {
                if (line.startsWith("PRETTY_NAME=")) {
                    return line.substring("PRETTY_NAME=".length()).replace("\"", "").strip();
                }
            }
        } catch (IOException e) {
            log.warn("Could not read {}: {}", OS_RELEASE_PATH, e.getMessage());
        }
        return "Unknown";
    }

    private String detectDesktop() {
        String desktop = System.getenv("XDG_CURRENT_DESKTOP");
        return desktop != null ? desktop : "Unknown";
    }

    private String detectAudioStack() {
        try {
            Process p = new ProcessBuilder("systemctl", "--user", "is-active", "pipewire").start();
            boolean finished = p.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
            if (finished && p.exitValue() == 0) return "PipeWire";
        } catch (IOException | InterruptedException e) {
            log.warn("Could not detect audio stack: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        return "Unknown";
    }

    private boolean detectGraphicalSession() {
        return System.getenv("DISPLAY") != null || System.getenv("WAYLAND_DISPLAY") != null;
    }
}
```

- [ ] **Step 2: Verify build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/
git commit -m "feat(infra): add Ubuntu24EnvironmentDetector"
```

---

### Task 12: Ubuntu24ActionCatalog (US-013)

**Files:**
- Create: `src/main/java/br/com/josenaldo/fixkitty/infrastructure/catalog/Ubuntu24ActionCatalog.java`
- Test: `src/test/java/br/com/josenaldo/fixkitty/infrastructure/catalog/Ubuntu24ActionCatalogTest.java`

- [ ] **Step 1: Write failing tests**

```java
package br.com.josenaldo.fixkitty.infrastructure.catalog;

import br.com.josenaldo.fixkitty.core.domain.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Ubuntu24ActionCatalogTest {

    Ubuntu24ActionCatalog catalog = new Ubuntu24ActionCatalog();
    EnvironmentProfile profile = new EnvironmentProfile("Ubuntu 24.04", "GNOME", "PipeWire", true);

    @Test
    void actionsFor_returnsAllSixActions() {
        List<RecoveryAction> actions = catalog.actionsFor(profile);
        assertEquals(6, actions.size());
    }

    @Test
    void planFor_fixAudio_returnsTwoSteps() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_AUDIO, profile);
        assertEquals(2, steps.size());
        assertFalse(steps.get(0).requiresPrivilege());
        assertFalse(steps.get(1).requiresPrivilege());
    }

    @Test
    void planFor_fixBluetooth_requiresPrivilege() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_BLUETOOTH, profile);
        assertEquals(1, steps.size());
        assertTrue(steps.get(0).requiresPrivilege());
        assertEquals(FailurePolicy.ABORT, steps.get(0).onFailure());
    }

    @Test
    void planFor_fixGnomeShell_noPrivilegeRequired() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_GNOME_SHELL, profile);
        assertEquals(1, steps.size());
        assertFalse(steps.get(0).requiresPrivilege());
    }

    @Test
    void planFor_fixAll_allStepsHaveContinuePolicy() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.FIX_ALL, profile);
        assertFalse(steps.isEmpty());
        steps.forEach(s -> assertEquals(FailurePolicy.CONTINUE, s.onFailure(),
            "FIX_ALL step " + s.id() + " must use CONTINUE policy"));
    }

    @Test
    void planFor_checkEnvironment_returnsEmptyList() {
        List<ExecutionStep> steps = catalog.planFor(RecoveryAction.CHECK_ENVIRONMENT, profile);
        assertTrue(steps.isEmpty());
    }

    @Test
    void defaultRecommendationFor_allActionsExceptCheckEnvironment_nonNull() {
        for (RecoveryAction action : RecoveryAction.values()) {
            if (action == RecoveryAction.CHECK_ENVIRONMENT) continue;
            assertNotNull(catalog.defaultRecommendationFor(action),
                "Recommendation must not be null for " + action);
        }
    }
}
```

- [ ] **Step 2: Run tests to confirm failure**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.infrastructure.catalog.Ubuntu24ActionCatalogTest"
```

Expected: FAIL.

- [ ] **Step 3: Create `Ubuntu24ActionCatalog.java`**

```java
package br.com.josenaldo.fixkitty.infrastructure.catalog;

import br.com.josenaldo.fixkitty.core.domain.*;
import br.com.josenaldo.fixkitty.core.ports.ActionCatalog;
import java.util.Arrays;
import java.util.List;

/**
 * Action catalog for Ubuntu 24 with GNOME, PipeWire, and NetworkManager.
 *
 * <p>Provides concrete {@link ExecutionStep} definitions for all six
 * recovery actions. {@link RecoveryAction#FIX_ALL} creates independent
 * steps with {@link FailurePolicy#CONTINUE}, overriding the individual
 * action policies to ensure all subsystems are attempted.
 */
public class Ubuntu24ActionCatalog implements ActionCatalog {

    @Override
    public List<RecoveryAction> actionsFor(EnvironmentProfile profile) {
        return Arrays.asList(RecoveryAction.values());
    }

    @Override
    public List<ExecutionStep> planFor(RecoveryAction action, EnvironmentProfile profile) {
        return switch (action) {
            case FIX_AUDIO -> List.of(
                step("audio-pipewire", "Restart PipeWire",
                    new String[]{"systemctl", "--user", "restart", "pipewire"},
                    false, 10, FailurePolicy.CONTINUE),
                step("audio-wireplumber", "Restart WirePlumber",
                    new String[]{"systemctl", "--user", "restart", "wireplumber"},
                    false, 10, FailurePolicy.CONTINUE)
            );
            case FIX_BLUETOOTH -> List.of(
                step("bt-restart", "Restart Bluetooth service",
                    new String[]{"systemctl", "restart", "bluetooth"},
                    true, 15, FailurePolicy.ABORT)
            );
            case FIX_NETWORK -> List.of(
                step("net-restart", "Restart NetworkManager",
                    new String[]{"systemctl", "restart", "NetworkManager"},
                    true, 20, FailurePolicy.ABORT)
            );
            case FIX_GNOME_SHELL -> List.of(
                step("gnome-killall", "Restart GNOME Shell",
                    new String[]{"killall", "gnome-shell"},
                    false, 10, FailurePolicy.ABORT)
            );
            case FIX_ALL -> List.of(
                // All steps use CONTINUE — FIX_ALL should attempt all subsystems
                step("all-audio-pipewire", "Restart PipeWire",
                    new String[]{"systemctl", "--user", "restart", "pipewire"},
                    false, 10, FailurePolicy.CONTINUE),
                step("all-audio-wireplumber", "Restart WirePlumber",
                    new String[]{"systemctl", "--user", "restart", "wireplumber"},
                    false, 10, FailurePolicy.CONTINUE),
                step("all-bt-restart", "Restart Bluetooth service",
                    new String[]{"systemctl", "restart", "bluetooth"},
                    true, 15, FailurePolicy.CONTINUE),
                step("all-net-restart", "Restart NetworkManager",
                    new String[]{"systemctl", "restart", "NetworkManager"},
                    true, 20, FailurePolicy.CONTINUE),
                step("all-gnome-killall", "Restart GNOME Shell",
                    new String[]{"killall", "gnome-shell"},
                    false, 10, FailurePolicy.CONTINUE)
            );
            case CHECK_ENVIRONMENT -> List.of();
        };
    }

    @Override
    public String defaultRecommendationFor(RecoveryAction action) {
        return switch (action) {
            case FIX_AUDIO -> "Try logging out and logging back in if audio is still broken.";
            case FIX_BLUETOOTH -> "Try toggling Bluetooth off and on in system settings.";
            case FIX_NETWORK -> "Try disconnecting and reconnecting to your network.";
            case FIX_GNOME_SHELL -> "If the issue persists, try a full logout and login.";
            case FIX_ALL -> "If issues persist, consider a full logout or system restart.";
            case CHECK_ENVIRONMENT -> null;
        };
    }

    private ExecutionStep step(String id, String desc, String[] cmd,
                               boolean priv, int timeout, FailurePolicy policy) {
        return new ExecutionStep(id, desc, cmd, priv, timeout, policy);
    }
}
```

- [ ] **Step 4: Run tests — confirm PASS**

```bash
./gradlew test --tests "br.com.josenaldo.fixkitty.infrastructure.catalog.Ubuntu24ActionCatalogTest"
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat(infra): add Ubuntu24ActionCatalog with all 6 recovery actions"
```

---

### Task 13: Wire AppModule and verify end-to-end (US-003 update)

**Files:**
- Modify: `src/main/java/br/com/josenaldo/fixkitty/bootstrap/AppModule.java`

- [ ] **Step 1: Update `AppModule.java` with all bindings**

```java
package br.com.josenaldo.fixkitty.bootstrap;

import br.com.josenaldo.fixkitty.core.ports.*;
import br.com.josenaldo.fixkitty.infrastructure.catalog.Ubuntu24ActionCatalog;
import br.com.josenaldo.fixkitty.infrastructure.command.ProcessBuilderCommandRunner;
import br.com.josenaldo.fixkitty.infrastructure.detectors.Ubuntu24EnvironmentDetector;
import br.com.josenaldo.fixkitty.infrastructure.privilege.*;
import com.google.inject.AbstractModule;

/**
 * Guice dependency injection module for FixKitty.
 *
 * <p>Binds all port interfaces to their Ubuntu 24 implementations.
 * Selects the appropriate {@link PrivilegeManager} based on the active interface.
 */
public class AppModule extends AbstractModule {

    private final boolean tuiMode;

    /**
     * Creates an AppModule for the specified interface mode.
     *
     * @param tuiMode {@code true} to configure TUI bindings (sudo); {@code false} for GUI (pkexec)
     */
    public AppModule(boolean tuiMode) {
        this.tuiMode = tuiMode;
    }

    @Override
    protected void configure() {
        bind(CommandRunner.class).to(ProcessBuilderCommandRunner.class);
        bind(EnvironmentDetector.class).to(Ubuntu24EnvironmentDetector.class);
        bind(ActionCatalog.class).to(Ubuntu24ActionCatalog.class);

        if (tuiMode) {
            bind(PrivilegeManager.class).to(SudoPrivilegeManager.class);
        } else {
            bind(PrivilegeManager.class).to(PkexecPrivilegeManager.class);
        }
    }
}
```

- [ ] **Step 2: Run full test suite**

```bash
./gradlew test
```

Expected: all PASS.

- [ ] **Step 3: Commit**

```bash
git add src/
git commit -m "feat(bootstrap): wire all infrastructure bindings in AppModule"
```

---

## Phase 1.4 — TUI (Lanterna)

### Task 14: TUI scaffold — TuiApp and EnvironmentScreen (US-014, US-017)

**Files:**
- Create: `TuiApp.java`, `EnvironmentScreen.java`

- [ ] **Step 1: Create `EnvironmentScreen.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.CheckEnvironmentUseCase;
import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

/**
 * TUI screen that displays the detected environment profile.
 *
 * <p>Calls {@link CheckEnvironmentUseCase} on display and renders each
 * profile field as a labelled row.
 *
 * <p>Lanterna classes are confined to this package and must not be imported
 * by any other layer.
 */
class EnvironmentScreen {

    private final CheckEnvironmentUseCase checkEnvironmentUseCase;

    /**
     * Creates the screen with the required use case.
     *
     * @param checkEnvironmentUseCase the use case used to detect the environment
     */
    EnvironmentScreen(CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
    }

    /**
     * Displays the environment profile in a Lanterna dialog.
     *
     * @param gui the active window-based text GUI
     */
    void show(WindowBasedTextGUI gui) {
        EnvironmentProfile profile = checkEnvironmentUseCase.execute();
        String content = String.format(
            "Distro:            %s%nDesktop:           %s%nAudio Stack:       %s%nGraphical Session: %s",
            profile.distro(), profile.desktop(), profile.audioStack(),
            profile.hasGraphicalSession() ? "Yes" : "No");

        new MessageDialogBuilder()
            .setTitle("Environment Profile")
            .setText(content)
            .addButton(MessageDialogButton.OK)
            .build()
            .showDialog(gui);
    }
}
```

- [ ] **Step 2: Create `TuiApp.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.*;
import com.google.inject.Inject;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the FixKitty terminal user interface.
 *
 * <p>Initialises a Lanterna screen and launches the main menu.
 * All Lanterna imports are confined to {@code interfaces/tui}.
 */
public class TuiApp {

    private static final Logger log = LoggerFactory.getLogger(TuiApp.class);

    private final ExecuteRecoveryUseCase executeUseCase;
    private final ListActionsUseCase listActionsUseCase;
    private final CheckEnvironmentUseCase checkEnvironmentUseCase;

    /**
     * Creates the TUI application with all required use cases.
     *
     * @param executeUseCase         executes recovery actions
     * @param listActionsUseCase     lists available actions
     * @param checkEnvironmentUseCase detects the environment
     */
    @Inject
    public TuiApp(ExecuteRecoveryUseCase executeUseCase,
                  ListActionsUseCase listActionsUseCase,
                  CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.executeUseCase = executeUseCase;
        this.listActionsUseCase = listActionsUseCase;
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
    }

    /**
     * Starts the TUI application, blocking until the user exits.
     */
    public void start() {
        try {
            Screen screen = new DefaultTerminalFactory().createScreen();
            screen.startScreen();

            MainMenuScreen menu = new MainMenuScreen(
                executeUseCase, listActionsUseCase, checkEnvironmentUseCase);
            menu.show(screen);

            screen.stopScreen();
        } catch (Exception e) {
            log.error("TUI error: {}", e.getMessage(), e);
            System.err.println("TUI error: " + e.getMessage());
        }
    }
}
```

- [ ] **Step 3: Verify build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/
git commit -m "feat(tui): add TuiApp and EnvironmentScreen scaffold"
```

---

### Task 15: TUI screens — MainMenu, Execution, Result (US-014, US-015, US-016)

**Files:**
- Create: `MainMenuScreen.java`, `ExecutionScreen.java`, `ResultScreen.java`

- [ ] **Step 1: Create `ResultScreen.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.core.domain.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;

/**
 * TUI screen that displays the result of a completed recovery action.
 *
 * <p>Shows overall status, individual step results, and recommendation.
 */
class ResultScreen {

    /**
     * Displays the execution result in a Lanterna dialog.
     *
     * @param gui    the active window-based text GUI
     * @param result the result to display
     */
    void show(WindowBasedTextGUI gui, ExecutionResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(result.status()).append("\n\n");

        for (StepResult step : result.steps()) {
            String icon = switch (step.status()) {
                case SUCCESS -> "[OK]";
                case FAILED -> "[FAIL]";
                case TIMEOUT -> "[TIMEOUT]";
                case SKIPPED -> "[SKIPPED]";
            };
            sb.append(icon).append(" ").append(step.step().description()).append("\n");
            if (step.status() == StepStatus.FAILED || step.status() == StepStatus.TIMEOUT) {
                if (!step.stderr().isBlank()) {
                    sb.append("  └─ ").append(step.stderr().lines().findFirst().orElse("")).append("\n");
                }
            }
        }

        if (result.recommendation() != null) {
            sb.append("\nRecommendation:\n").append(result.recommendation());
        }

        new MessageDialogBuilder()
            .setTitle("Result: " + result.action().displayName())
            .setText(sb.toString())
            .addButton(MessageDialogButton.OK)
            .build()
            .showDialog(gui);
    }
}
```

- [ ] **Step 2: Create `ExecutionScreen.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.ExecuteRecoveryUseCase;
import br.com.josenaldo.fixkitty.core.domain.*;
import com.googlecode.lanterna.gui2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TUI screen that runs a recovery action and shows progress.
 *
 * <p>Executes the use case synchronously and delegates result rendering
 * to {@link ResultScreen}.
 */
class ExecutionScreen {

    private static final Logger log = LoggerFactory.getLogger(ExecutionScreen.class);
    private final ExecuteRecoveryUseCase executeUseCase;
    private final ResultScreen resultScreen;

    /**
     * Creates the execution screen.
     *
     * @param executeUseCase the use case for executing recovery actions
     * @param resultScreen   the screen for rendering results
     */
    ExecutionScreen(ExecuteRecoveryUseCase executeUseCase, ResultScreen resultScreen) {
        this.executeUseCase = executeUseCase;
        this.resultScreen = resultScreen;
    }

    /**
     * Executes the given action and shows the result.
     *
     * @param gui    the active window-based text GUI
     * @param action the action to execute
     */
    void run(WindowBasedTextGUI gui, RecoveryAction action) {
        log.info("Executing action: {}", action);
        // Phase 1: synchronous execution (progress shown after completion)
        ExecutionResult result = executeUseCase.execute(action);
        resultScreen.show(gui, result);
    }
}
```

- [ ] **Step 3: Create `MainMenuScreen.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.tui;

import br.com.josenaldo.fixkitty.application.*;
import br.com.josenaldo.fixkitty.core.domain.*;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.menu.*;
import com.googlecode.lanterna.screen.Screen;

import java.util.List;

/**
 * TUI main menu screen.
 *
 * <p>Lists all available recovery actions and the environment profile in the header.
 * Delegates action execution to {@link ExecutionScreen} and environment display
 * to {@link EnvironmentScreen}.
 */
class MainMenuScreen {

    private final ExecuteRecoveryUseCase executeUseCase;
    private final ListActionsUseCase listActionsUseCase;
    private final CheckEnvironmentUseCase checkEnvironmentUseCase;

    /**
     * Creates the main menu screen.
     *
     * @param executeUseCase          executes recovery actions
     * @param listActionsUseCase      lists available actions
     * @param checkEnvironmentUseCase detects environment
     */
    MainMenuScreen(ExecuteRecoveryUseCase executeUseCase,
                   ListActionsUseCase listActionsUseCase,
                   CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.executeUseCase = executeUseCase;
        this.listActionsUseCase = listActionsUseCase;
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
    }

    /**
     * Displays the main menu, blocking until the user exits.
     *
     * @param screen the active Lanterna screen
     */
    void show(Screen screen) {
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
        EnvironmentProfile profile = checkEnvironmentUseCase.execute();
        List<RecoveryAction> actions = listActionsUseCase.execute();

        ExecutionScreen executionScreen = new ExecutionScreen(executeUseCase, new ResultScreen());
        EnvironmentScreen environmentScreen = new EnvironmentScreen(checkEnvironmentUseCase);

        BasicWindow window = new BasicWindow("FixKitty — " + profile.distro() + " / " + profile.desktop());
        window.setHints(List.of(Window.Hint.FULL_SCREEN));

        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        panel.addComponent(new Label("  Graphical: " + (profile.hasGraphicalSession() ? "Yes" : "No")
            + "  |  Audio: " + profile.audioStack()));
        panel.addComponent(new EmptySpace());

        for (RecoveryAction action : actions) {
            RecoveryAction captured = action;
            Button btn = new Button(action.displayName(), () -> {
                if (captured == RecoveryAction.CHECK_ENVIRONMENT) {
                    environmentScreen.show(gui);
                } else {
                    executionScreen.run(gui, captured);
                }
            });
            panel.addComponent(btn);
        }

        panel.addComponent(new EmptySpace());
        panel.addComponent(new Button("Exit", window::close));

        window.setComponent(panel);
        gui.addWindowAndWait(window);
    }
}
```

- [ ] **Step 4: Wire TuiApp into Main**

Update `Main.java` TUI branch:

```java
if (tuiMode) {
    log.info("Launching TUI");
    TuiApp tuiApp = injector.getInstance(TuiApp.class);
    tuiApp.start();
}
```

Add import: `import br.com.josenaldo.fixkitty.interfaces.tui.TuiApp;`

- [ ] **Step 5: Build and verify**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Manual test — TUI**

```bash
./gradlew run --args="--tui"
```

Expected: Lanterna menu appears with all actions listed.

Check test cases: TC-TUI-001, TC-TUI-002, TC-TUI-011.

- [ ] **Step 7: Commit**

```bash
git add src/
git commit -m "feat(tui): add MainMenuScreen, ExecutionScreen, ResultScreen — full TUI operational"
```

---

## Phase 1.5 — GUI (JavaFX)

### Task 16: GuiApp and EnvironmentPanel (US-018, US-023)

**Files:**
- Create: `GuiApp.java`, `EnvironmentPanel.java`

- [ ] **Step 1: Create `EnvironmentPanel.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.application.CheckEnvironmentUseCase;
import br.com.josenaldo.fixkitty.core.domain.EnvironmentProfile;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * JavaFX panel that displays the detected Linux environment profile.
 *
 * <p>Populated on startup and refreshed when the user triggers Check Environment.
 * Contains no business logic — delegates detection to {@link CheckEnvironmentUseCase}.
 */
public class EnvironmentPanel extends VBox {

    private final CheckEnvironmentUseCase checkEnvironmentUseCase;
    private final Label distroLabel = new Label();
    private final Label desktopLabel = new Label();
    private final Label audioLabel = new Label();
    private final Label sessionLabel = new Label();

    /**
     * Creates the panel with the required use case.
     *
     * @param checkEnvironmentUseCase the use case for detecting the environment
     */
    public EnvironmentPanel(CheckEnvironmentUseCase checkEnvironmentUseCase) {
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
        setPadding(new Insets(8));
        setSpacing(4);
        getChildren().addAll(
            new Label("Environment"),
            distroLabel, desktopLabel, audioLabel, sessionLabel);
        refresh();
    }

    /**
     * Calls {@link CheckEnvironmentUseCase} and updates all displayed fields.
     *
     * <p>Must be called on the JavaFX Application Thread.
     */
    public void refresh() {
        EnvironmentProfile profile = checkEnvironmentUseCase.execute();
        distroLabel.setText("Distro:    " + profile.distro());
        desktopLabel.setText("Desktop:   " + profile.desktop());
        audioLabel.setText("Audio:     " + profile.audioStack());
        sessionLabel.setText("Session:   " + (profile.hasGraphicalSession() ? "Yes" : "No"));
    }
}
```

- [ ] **Step 2: Create `GuiApp.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.gui;

import atlantafx.base.theme.Dracula;
import br.com.josenaldo.fixkitty.application.*;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for FixKitty.
 *
 * <p>Applies the AtlantaFX Dracula theme and constructs the main window
 * from injected use cases. The Guice injector is passed via a static field
 * set by {@code Main} before {@code launch()} is called.
 */
public class GuiApp extends Application {

    /** Set by Main before Application.launch() is called. */
    static Injector injector;

    /**
     * Starts the JavaFX application, applies the Dracula theme, and shows the main window.
     *
     * @param stage the primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        ExecuteRecoveryUseCase executeUseCase = injector.getInstance(ExecuteRecoveryUseCase.class);
        ListActionsUseCase listActionsUseCase = injector.getInstance(ListActionsUseCase.class);
        CheckEnvironmentUseCase checkEnvironmentUseCase = injector.getInstance(CheckEnvironmentUseCase.class);

        MainController controller = new MainController(
            executeUseCase, listActionsUseCase, checkEnvironmentUseCase, stage);

        Scene scene = new Scene(controller.buildLayout(), 800, 600);
        stage.setTitle("FixKitty");
        stage.setScene(scene);
        stage.show();
    }
}
```

- [ ] **Step 3: Update `Main.java` GUI branch**

```java
} else {
    log.info("Launching GUI");
    GuiApp.injector = injector;
    javafx.application.Application.launch(GuiApp.class, args);
}
```

Add import: `import br.com.josenaldo.fixkitty.interfaces.gui.GuiApp;`

- [ ] **Step 4: Verify build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add src/
git commit -m "feat(gui): add GuiApp with Dracula theme and EnvironmentPanel"
```

---

### Task 17: LogPanel and ResultPanel (US-019, US-020)

**Files:**
- Create: `LogPanel.java`, `ResultPanel.java`

- [ ] **Step 1: Create `LogPanel.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.core.domain.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * JavaFX panel that displays real-time step progress during execution.
 *
 * <p>Must be updated on the JavaFX Application Thread via {@link Platform#runLater}.
 */
public class LogPanel extends VBox {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_STDERR_LINES = 10;

    private final TextArea area = new TextArea();

    /**
     * Creates a scrollable, read-only log panel.
     */
    public LogPanel() {
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(12);
        getChildren().add(area);
    }

    /**
     * Clears the log content. Call before starting a new action.
     * Must be called on the JavaFX Application Thread.
     */
    public void clear() {
        area.clear();
    }

    /**
     * Appends a step result to the log.
     * Must be called on the JavaFX Application Thread.
     *
     * @param result the step result to display
     */
    public void appendStep(StepResult result) {
        String time = LocalTime.now().format(TIME_FMT);
        String icon = switch (result.status()) {
            case SUCCESS -> "[OK]";
            case FAILED -> "[FAIL]";
            case TIMEOUT -> "[TIMEOUT]";
            case SKIPPED -> "[SKIPPED]";
        };
        area.appendText(String.format("%s %s %s%n", time, icon, result.step().description()));

        if (result.status() == StepStatus.FAILED || result.status() == StepStatus.TIMEOUT) {
            if (!result.stderr().isBlank()) {
                String[] lines = result.stderr().split("\n");
                int shown = Math.min(lines.length, MAX_STDERR_LINES);
                for (int i = 0; i < shown; i++) {
                    area.appendText("      " + lines[i] + "\n");
                }
                if (lines.length > MAX_STDERR_LINES) {
                    area.appendText("      ... (truncated)\n");
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create `ResultPanel.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.core.domain.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * JavaFX panel that displays the overall result of a recovery action.
 *
 * <p>Shows overall status with colour coding and a recommendation message
 * when the action did not fully succeed.
 */
public class ResultPanel extends VBox {

    private final Label statusLabel = new Label();
    private final Label recommendationLabel = new Label();

    /**
     * Creates an initially empty result panel.
     */
    public ResultPanel() {
        setPadding(new Insets(8));
        setSpacing(4);
        recommendationLabel.setWrapText(true);
        getChildren().addAll(statusLabel, recommendationLabel);
    }

    /**
     * Updates the panel to display the given execution result.
     * Must be called on the JavaFX Application Thread.
     *
     * @param result the result to display
     */
    public void display(ExecutionResult result) {
        statusLabel.setText("Status: " + result.status());
        statusLabel.setStyle(switch (result.status()) {
            case SUCCESS -> "-fx-text-fill: #50fa7b;";  // green
            case PARTIAL -> "-fx-text-fill: #ffb86c;";  // orange
            case FAILED -> "-fx-text-fill: #ff5555;";   // red
        });

        if (result.recommendation() != null) {
            recommendationLabel.setText("Recommendation: " + result.recommendation());
            recommendationLabel.setVisible(true);
        } else {
            recommendationLabel.setVisible(false);
        }
    }

    /**
     * Clears the panel. Call before starting a new action.
     */
    public void clear() {
        statusLabel.setText("");
        recommendationLabel.setVisible(false);
    }
}
```

- [ ] **Step 3: Verify build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/
git commit -m "feat(gui): add LogPanel and ResultPanel"
```

---

### Task 18: AppRestarter and GnomeShellConfirmationDialog (US-021, US-022)

**Files:**
- Create: `AppRestarter.java`, `GnomeShellConfirmationDialog.java`

- [ ] **Step 1: Create `AppRestarter.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.gui;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Relaunches the FixKitty process and terminates the current JVM.
 *
 * <p>Used after Fix GNOME Shell to bring the application back once
 * the GNOME shell has restarted. Waits 3 seconds before relaunching
 * to allow the shell to fully restart.
 */
public class AppRestarter {

    private static final Logger log = LoggerFactory.getLogger(AppRestarter.class);

    /**
     * Relaunches FixKitty in a background process and exits the current JVM.
     *
     * <p>Uses the same Java executable detected via {@link ProcessHandle}.
     * If detection fails, logs a warning and exits without relaunching.
     */
    public void relaunch() {
        ProcessHandle current = ProcessHandle.current();
        String javaCmd = current.info().command().orElse(null);

        if (javaCmd == null) {
            log.warn("Could not determine java executable path — skipping relaunch");
            Platform.exit();
            System.exit(0);
            return;
        }

        // Build relaunch command: wait 3s for GNOME shell to settle, then relaunch
        String relaunchScript = "sleep 3 && nohup " + javaCmd
            + " -jar " + getJarPath() + " &";

        try {
            new ProcessBuilder("bash", "-c", relaunchScript)
                .inheritIO()
                .start();
            log.info("Relaunch scheduled: {}", relaunchScript);
        } catch (Exception e) {
            log.error("Failed to schedule relaunch: {}", e.getMessage());
        }

        Platform.exit();
        System.exit(0);
    }

    private String getJarPath() {
        // For Phase 1 (Gradle run), the jar path is approximated
        // This will be replaced with proper jpackage path in Phase 5
        return System.getProperty("user.dir") + "/build/libs/fixkitty.jar";
    }
}
```

- [ ] **Step 2: Create `GnomeShellConfirmationDialog.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.gui;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import java.util.Optional;

/**
 * Confirmation dialog shown before executing Fix GNOME Shell.
 *
 * <p>Warns the user that the graphical interface will briefly close and
 * FixKitty will relaunch automatically. Requires explicit confirmation.
 */
public class GnomeShellConfirmationDialog {

    /**
     * Shows the confirmation dialog and returns {@code true} if the user confirms.
     *
     * @return {@code true} if the user clicked "Confirm"; {@code false} if cancelled
     */
    public boolean confirm() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Fix GNOME Shell");
        alert.setHeaderText("This action will restart the GNOME shell.");
        alert.setContentText(
            "The graphical interface will briefly close and FixKitty " +
            "will relaunch automatically. Continue?");

        ButtonType confirmBtn = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(confirmBtn, cancelBtn);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmBtn;
    }
}
```

- [ ] **Step 3: Verify build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/
git commit -m "feat(gui): add AppRestarter and GnomeShellConfirmationDialog"
```

---

### Task 19: MainController — wire everything together (US-018)

**Files:**
- Create: `src/main/java/br/com/josenaldo/fixkitty/interfaces/gui/MainController.java`

- [ ] **Step 1: Create `MainController.java`**

```java
package br.com.josenaldo.fixkitty.interfaces.gui;

import br.com.josenaldo.fixkitty.application.*;
import br.com.josenaldo.fixkitty.core.domain.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Main JavaFX controller for FixKitty.
 *
 * <p>Builds the application layout and wires action buttons to use cases.
 * Contains no business logic — all execution is delegated to use cases.
 * Buttons are disabled during execution to prevent concurrent actions.
 */
public class MainController {

    private final ExecuteRecoveryUseCase executeUseCase;
    private final ListActionsUseCase listActionsUseCase;
    private final CheckEnvironmentUseCase checkEnvironmentUseCase;
    private final Stage stage;

    private final EnvironmentPanel environmentPanel;
    private final LogPanel logPanel;
    private final ResultPanel resultPanel;
    private final GnomeShellConfirmationDialog gnomeConfirmDialog;
    private final AppRestarter appRestarter;

    private List<Button> actionButtons;

    /**
     * Creates the main controller with all required dependencies.
     *
     * @param executeUseCase          executes recovery actions
     * @param listActionsUseCase      lists available actions
     * @param checkEnvironmentUseCase detects the current environment
     * @param stage                   the primary JavaFX stage
     */
    public MainController(ExecuteRecoveryUseCase executeUseCase,
                          ListActionsUseCase listActionsUseCase,
                          CheckEnvironmentUseCase checkEnvironmentUseCase,
                          Stage stage) {
        this.executeUseCase = executeUseCase;
        this.listActionsUseCase = listActionsUseCase;
        this.checkEnvironmentUseCase = checkEnvironmentUseCase;
        this.stage = stage;
        this.environmentPanel = new EnvironmentPanel(checkEnvironmentUseCase);
        this.logPanel = new LogPanel();
        this.resultPanel = new ResultPanel();
        this.gnomeConfirmDialog = new GnomeShellConfirmationDialog();
        this.appRestarter = new AppRestarter();
    }

    /**
     * Builds and returns the root layout node.
     *
     * @return the root {@link BorderPane} for the main scene
     */
    public BorderPane buildLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        // Left sidebar: environment + action buttons
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(0, 12, 0, 0));
        sidebar.setPrefWidth(200);
        sidebar.getChildren().add(environmentPanel);
        sidebar.getChildren().add(new Separator(Orientation.HORIZONTAL));

        List<RecoveryAction> actions = listActionsUseCase.execute();
        actionButtons = actions.stream().map(this::createButton).toList();
        sidebar.getChildren().addAll(actionButtons);

        // Center: log + result
        VBox center = new VBox(8);
        center.getChildren().addAll(logPanel, resultPanel);

        root.setLeft(sidebar);
        root.setCenter(center);
        return root;
    }

    private Button createButton(RecoveryAction action) {
        FontIcon icon = switch (action) {
            case FIX_AUDIO -> FontIcon.of(FontAwesomeSolid.VOLUME_UP);
            case FIX_BLUETOOTH -> FontIcon.of(FontAwesomeSolid.BLUETOOTH_B);
            case FIX_NETWORK -> FontIcon.of(FontAwesomeSolid.WIFI);
            case FIX_GNOME_SHELL -> FontIcon.of(FontAwesomeSolid.DESKTOP);
            case FIX_ALL -> FontIcon.of(FontAwesomeSolid.TOOLS);
            case CHECK_ENVIRONMENT -> FontIcon.of(FontAwesomeSolid.INFO_CIRCLE);
        };

        Button btn = new Button(action.displayName(), icon);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> handleAction(action));
        return btn;
    }

    private void handleAction(RecoveryAction action) {
        if (action == RecoveryAction.CHECK_ENVIRONMENT) {
            environmentPanel.refresh();
            return;
        }
        if (action == RecoveryAction.FIX_GNOME_SHELL) {
            if (!gnomeConfirmDialog.confirm()) return;
            runAction(action, true);
            return;
        }
        runAction(action, false);
    }

    private void runAction(RecoveryAction action, boolean relaunchAfter) {
        setButtonsDisabled(true);
        logPanel.clear();
        resultPanel.clear();

        Executors.newVirtualThreadPerTaskExecutor().execute(() -> {
            ExecutionResult result = executeUseCase.execute(action);
            Platform.runLater(() -> {
                result.steps().forEach(logPanel::appendStep);
                if (relaunchAfter) {
                    appRestarter.relaunch();
                } else {
                    resultPanel.display(result);
                    setButtonsDisabled(false);
                }
            });
        });
    }

    private void setButtonsDisabled(boolean disabled) {
        if (actionButtons != null) {
            actionButtons.forEach(b -> b.setDisable(disabled));
        }
    }
}
```

- [ ] **Step 2: Build**

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Launch GUI and run manual tests**

```bash
./gradlew run
```

Manual test checklist (see `docs/tests/gui-test-cases.md`):

- [ ] TC-GUI-001: Window opens, all 6 buttons visible with icons and Dracula theme
- [ ] TC-GUI-010: Click Check Environment — environment panel refreshes
- [ ] TC-GUI-011: Environment panel populated on startup (no click needed)
- [ ] TC-GUI-012: Click Fix Audio — buttons disabled during execution
- [ ] TC-GUI-002: Fix Audio succeeds — log shows OK, status shows SUCCESS
- [ ] TC-GUI-006: Fix GNOME Shell → Cancel → nothing happens
- [ ] TC-GUI-007: Fix GNOME Shell → Confirm → executes and relaunches

- [ ] **Step 4: Commit**

```bash
git add src/
git commit -m "feat(gui): add MainController — full GUI operational with all action buttons"
```

---

## Phase 1.6 — Final Validation

### Task 20: Architecture validation and full test run (US all)

- [ ] **Step 1: Run full test suite**

```bash
./gradlew test
```

Expected: all tests PASS.

- [ ] **Step 2: Architecture check — no interface imports in core or application**

```bash
grep -r "interfaces\." src/main/java/br/com/josenaldo/fixkitty/core/ && echo "VIOLATION" || echo "OK"
grep -r "interfaces\." src/main/java/br/com/josenaldo/fixkitty/application/ && echo "VIOLATION" || echo "OK"
```

Expected: both print `OK`

- [ ] **Step 3: Architecture check — no Lanterna imports outside tui**

```bash
grep -r "com.googlecode.lanterna" src/main/java/br/com/josenaldo/fixkitty/ \
  --include="*.java" \
  | grep -v "interfaces/tui" && echo "VIOLATION" || echo "OK"
```

Expected: `OK`

- [ ] **Step 4: Architecture check — no hardcoded systemctl/sudo outside infrastructure**

```bash
grep -r "systemctl\|sudo\|pkexec\|killall" \
  src/main/java/br/com/josenaldo/fixkitty/core/ \
  src/main/java/br/com/josenaldo/fixkitty/application/ \
  --include="*.java" && echo "VIOLATION" || echo "OK"
```

Expected: `OK`

- [ ] **Step 5: Javadoc check — verify all public types have Javadoc**

```bash
./gradlew javadoc 2>&1 | grep "warning" | grep -v "package-info" | head -20
```

Expected: no warnings about missing Javadoc on public types/methods.

- [ ] **Step 6: Manual test — TUI in real TTY**

Switch to TTY2: Ctrl+Alt+F2, login, navigate to project directory, run:

```bash
./gradlew run --args="--tui"
```

Test checklist (see `docs/tests/tui-test-cases.md`):

- [ ] TC-TUI-001: TUI launches, menu visible, environment header shown
- [ ] TC-TUI-002: Arrow keys and number keys navigate menu
- [ ] TC-TUI-010: Check Environment shows profile
- [ ] TC-TUI-003: Fix Audio — both steps shown, overall SUCCESS
- [ ] TC-TUI-013: Full session in TTY2 — all actions accessible

- [ ] **Step 7: Final commit**

```bash
./gradlew build
git add .
git commit -m "feat: Phase 1 complete — FixKitty MVP with GUI, TUI, and full Clean Architecture"
```

---

## Quick Reference

### Test commands

| What | Command |
| --- | --- |
| All tests | `./gradlew test` |
| Domain tests only | `./gradlew test --tests "br.com.josenaldo.fixkitty.core.domain.*"` |
| Application tests | `./gradlew test --tests "br.com.josenaldo.fixkitty.application.*"` |
| Infrastructure tests | `./gradlew test --tests "br.com.josenaldo.fixkitty.infrastructure.*"` |
| Run GUI | `./gradlew run` |
| Run TUI | `./gradlew run --args="--tui"` |
| Full build | `./gradlew build` |
| Javadoc | `./gradlew javadoc` |

### Key invariants to never break

- No import of `interfaces.*` in `core` or `application`
- No Lanterna import outside `interfaces/tui`
- No hardcoded `systemctl`/`sudo` outside `infrastructure/`
- Every public Java type and method must have Javadoc
- `ExecutionResult` never throws for execution failures
- GUI controllers contain no business logic
