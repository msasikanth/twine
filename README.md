# Twine - RSS Reader

![CI-MAIN](https://github.com/msasikanth/twine/actions/workflows/ci_checks.yml/badge.svg?branch=main)
[![Sponsor](https://img.shields.io/static/v1?label=Sponsor&message=%E2%9D%A4&logo=GitHub&color=%23fe8e86)](https://github.com/sponsors/msasikanth)

Twine is a cross-platform RSS reader app built using Kotlin and Compose Multiplatform. It features an nice
user interface and experience to browse through the feeds, and supports Material 3 content based 
[dynamic theming](https://m3.material.io/styles/color/dynamic-color/user-generated-color).

## Download

<a href='https://play.google.com/store/apps/details?id=dev.sasikanth.rss.reader&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="200px"/></a>
<a href="https://apps.apple.com/us/app/twine-rss-reader/id6465694958?itsct=apps_box_badge&amp;itscg=30200" style="display: inline-block; overflow: hidden; border-radius: 13px;"><img src="https://tools.applemediaservices.com/api/badges/download-on-the-app-store/white/en-us;releaseDate=1694390400" alt="Download on the App Store" width="200px"></a>

## Screenshots

### Light
<p style="text-align: center;">
  <img src="readme_images/light/home_screen.png" width="250" alt="Home screen"/>
  <img src="readme_images/light/home_screen_filters.png" width="250" alt="Home screen filters"/>
  <img src="readme_images/light/home_screen_scrolled.png" width="250" alt="Home screen scrolled"/>
  <img src="readme_images/light/reader_screen.png" width="250" alt="Reader screen"/>
  <img src="readme_images/light/search_screen.png" width="250" alt="Search screen"/>
  <img src="readme_images/light/settings_1.png" width="250" alt="Settings screen 1"/>
  <img src="readme_images/light/settings_2.png" width="250" alt="Settings screen 2"/>
  <img src="readme_images/light/expanded_bottom_sheet_feed_1.png" width="250" alt="Expanded feeds sheet 1"/>
  <img src="readme_images/light/expanded_bottom_sheet_feed_2.png" width="250" alt="Expanded feeds sheet 2"/>
  <img src="readme_images/light/add_feed.png" width="250" alt="Add feed screen"/>
</p>

### Dark
<p style="text-align: center;">
  <img src="readme_images/dark/home_screen.png" width="250" alt="Home screen"/>
  <img src="readme_images/dark/home_screen_filters.png" width="250" alt="Home screen filters"/>
  <img src="readme_images/dark/home_screen_scrolled.png" width="250" alt="Home screen scrolled"/>
  <img src="readme_images/dark/reader_screen.png" width="250" alt="Reader screen"/>
  <img src="readme_images/dark/search_screen.png" width="250" alt="Search screen"/>
  <img src="readme_images/dark/settings_1.png" width="250" alt="Settings screen 1"/>
  <img src="readme_images/dark/settings_2.png" width="250" alt="Settings screen 2"/>
  <img src="readme_images/dark/expanded_bottom_sheet_feed_1.png" width="250" alt="Expanded feeds sheet 1"/>
  <img src="readme_images/dark/expanded_bottom_sheet_feed_2.png" width="250" alt="Expanded feeds sheet 2"/>
  <img src="readme_images/dark/add_feed.png" width="250" alt="Add feed screen"/>
</p>

## Features ✨

- Supports **RDF**, **RSS**, **Atom** and **JSON** feeds
- Feed management: Add, Edit, Remove & Pin feeds
- Feed grouping
- Access to pinned feeds/groups from bottom bar in the home screen
- Smart fetching: Twine looks for feeds when given any website homepage
- Article shortcut to fetch full article in reader view
- Bookmark posts to read later
- Search posts
- Background sync
- Import and exports your feeds with OPML
- Dynamic content theming
- Light/Dark mode support

## Tech Stack 📚

- [Kotlin Multiplatform](https://kotlinlang.org/lp/multiplatform/)
- [Kotlin Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor](https://ktor.io/)
- [SQLDelight](https://cashapp.github.io/sqldelight/2.0.0-alpha05/)
- [Decompose](https://arkivanov.github.io/Decompose/)
- [Kotlin-inject](https://github.com/evant/kotlin-inject)

For full list of dependencies used, please take a look at the [catalog](/gradle/libs.versions.toml) file.

## Development 🛠️

You can just clone the repo and build it locally without requiring any changes. 

Project requires JDK 20+, and based on the AGP version defined in [`libs.versions.toml`](/gradle/libs.versions.toml) file, 
you can use appropriate Android Studio to import the project.

## Contributing 🛠️

You can contribute bug fixes to the project via PRs, for anything else open an issue to start a conversation.

This project uses ktfmt, provided via the spotless gradle plugin, and the bundled project IntelliJ codestyle. Run
`./gradlew spotlessApply` to format the code before raising a PR.

## Made with 💖 by

- [Sasikanth Miriyampalli](https://www.sasikanth.dev) / Development
- [Eduardo Pratti](https://twitter.com/edpratti) / Design

## Error Reporting by

<a href="http://www.bugsnag.com/">
  <img src="readme_images/bugsnag.png" width="250" alt="bugsnag logo"/>
</a>

## License

```
Copyright Sasikanth Miriyampalli

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
