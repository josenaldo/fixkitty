# ADR-009: JavaFX 23 + AtlantaFX + Ikonli for GUI

**Date:** 2026-03-23
**Status:** Accepted

## Context

FixKitty requires a graphical desktop interface runnable on Linux (Ubuntu 24). The choice
of GUI framework affects distribution, styling capability, and maintenance burden.

## Decision

Use **JavaFX 23** as the GUI framework, **AtlantaFX** for theming (Dracula), and
**Ikonli** for vector icons.

**JavaFX** was chosen over Swing because:

- It is the modern Java GUI toolkit with active development (OpenJFX)
- Supports CSS-based styling natively
- Integrates with `jpackage` for `.deb`/`.rpm` packaging (relevant for future phases)
- Swing is considered legacy; JavaFX is better suited for a new project

**AtlantaFX** was chosen because:

- It provides production-quality dark themes (Dracula, Nord, Primer) with no custom CSS needed
- It is actively maintained and compatible with JavaFX 23
- The Dracula theme matches the visual identity of a technical recovery console

Other JavaFX theme libraries were considered:

- **JFoenix** — abandoned, not compatible with modern JavaFX
- **MaterialFX** — Material Design aesthetic is not appropriate for a sysadmin tool

**Ikonli** was chosen because:

- It integrates icon packs (Font Awesome, Material Design Icons) directly as JavaFX nodes
- No image files needed; icons are vector and scale cleanly
- Widely used in the JavaFX ecosystem

## Consequences

- JavaFX requires a separate runtime dependency (OpenJFX) declared in `build.gradle.kts`
- AtlantaFX and Ikonli are added as compile-time dependencies scoped to `interfaces/gui`
- Theme selection (Dracula) is applied once in `GuiApp` bootstrap — not in individual controllers
