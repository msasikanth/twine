# Twine - Project Context

## Overview
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
- `desktopApp`: Desktop-specific application code (Compose for Desktop).

## Tech Stack
- **Kotlin Multiplatform (KMP)**: Shared logic across Android, iOS, and Desktop.
- **Compose Multiplatform**: UI framework for shared screens.
- **SQLDelight**: Local database management.
- **Ktor**: HTTP client for network requests.
- **Kotlin-inject**: Dependency injection.
- **Coroutines & Flow**: For asynchronous programming.
- **Coil**: Image loading.
- **Multiplatform Markdown Renderer**: For rendering article content.

## Development Guidelines
- **JDK**: Requires JDK 21 or higher.
- **Code Style**: Uses `ktfmt` with Google style for formatting.
- **Spotless**: Managed via the Spotless Gradle plugin.
- **Formatting**: Always run `./gradlew spotlessApply` before committing changes.
- **Build**: Run `./gradlew buildFullDebug` for Android, and `gradle :shared:linkDebugFrameworkIosArm64` for iOS to compile the project for debug variant
- **Testing**: Tests are primarily located in `commonTest` directories within each module. Run tests using `./gradlew test`.
- **License**: GNU GPLv3 as specified in `LICENSE.txt`.

## Key Features
- Supports **RDF**, **RSS**, **Atom**, and **JSON** feeds.
- Feed management: Add, edit, delete, pin, and group feeds.
- Dynamic theming: Content based dynamic theming.
- Smart fetching: Discovers feeds from website homepages.
- Reader view: Article shortcut to fetch full article content.
- Audio player: For podcast feeds and HTML audio tags
- Bookmarks and Search.
- Content filtering: Blocked words based on keywords.
- Background sync and Cloud sync (FreshRSS, Miniflux, Dropbox).
- OPML import and export.

## PR instructions
- Title format: <Title>. Title should not include any emojis
- Always run `./gradlew spotlessApply` before committing.
