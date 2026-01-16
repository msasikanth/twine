# Project Guidelines

## Project Overview
Twine is a modern cross-platform RSS reader app built using Kotlin and Compose Multiplatform. It supports RDF, RSS, Atom, and JSON feeds and features Material 3 dynamic theming.

## Project Structure
The project follows a modular Kotlin Multiplatform structure:
- `androidApp`: Android-specific application code and entry point.
- `iosApp`: iOS-specific application code (Swift/Xcode project).
- `shared`: Contains the core UI logic using Compose Multiplatform, ViewModels (using `kotlin-inject`), and shared presentation logic.
- `core/`: Modularized business logic.
    - `base`: Base utilities, common interfaces, and platform abstractions (e.g., notifications).
    - `data`: Data layer containing repositories, local database (SQLDelight), and sync logic.
    - `model`: Domain models used across the project.
    - `network`: Network layer using Ktor for fetching feeds and parsing.
- `resources/icons`: Shared icon resources.

## Tech Stack
- **Kotlin Multiplatform (KMP)**: Shared logic across Android and iOS.
- **Compose Multiplatform**: UI framework for shared screens.
- **SQLDelight**: Local database management.
- **Ktor**: HTTP client for network requests.
- **Kotlin-inject**: Dependency injection.
- **Coroutines & Flow**: For asynchronous programming.
- **Coil**: Image loading.

## Coding Standards
- **Code Style**: The project uses `ktfmt` with Google style for formatting.
- **Spotless**: Managed via the Spotless Gradle plugin.
- **License Headers**: Files should include the appropriate license header.
- **Formatting**: Always run `./gradlew spotlessApply` before committing or submitting changes to ensure compliance.

## Testing
- Tests are primarily located in `commonTest` directories within each module (e.g., `core/network/src/commonTest`).
- Run tests using `./gradlew test` (runs all tests) or module-specific tasks like `./gradlew :core:network:allTests`.
- **Requirements**: When fixing bugs, create a reproduction test case. When adding features, ensure core logic is covered by tests.

## Workflow & Development
- **JDK**: Requires JDK 20 or higher.
- **Building**: Use `./gradlew assemble` for a general build.
- **Verification**: Run tests and `./gradlew spotlessApply` before finishing tasks.
- **Translations**: Strings are managed via Crowdin and located in `shared/src/commonMain/composeResources`.
