# Project Guidelines: مسار (Masar) - Android Native App

## Platform & Framework
- **Platform**: Native Android Application (Kotlin + Jetpack Compose)
- **Primary Toolchain**: Gradle (Kotlin DSL - `build.gradle.kts`), Jetpack Compose, Room Database, Material Design 3.
- **Preview Environment**: Android Streaming Emulator (Device Preview). This is NOT a web or React Native project.
- **Language & Direction**: Arabic (RTL) first, with full fintech simulation and deterministic financial modeling.

## Project Structure
- Root configuration: `settings.gradle.kts`, `build.gradle.kts`, `metadata.json`
- Android App module: `/app`
- Database: Room Database in `app/src/main/java/com/example/data/local/`
- UI: Jetpack Compose screens and components in `app/src/main/java/com/example/ui/`
- Domain: Deterministic Financial Engine, Scenario Engine, and Decision Intelligence in `app/src/main/java/com/example/domain/`

## Instructions for AI Agent & Build System
1. Always compile and test using the Android Gradle build system (`compile_applet` or `gradle :app:assembleDebug`).
2. Maintain `metadata.json` with `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`.
3. Never convert or treat this repository as a Web/Node.js application.
