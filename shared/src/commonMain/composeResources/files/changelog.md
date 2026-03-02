## Features

- **Redesigned Settings**: Settings have been refactored into sub-screens with a fresh, modern UI.
- **Audio Player**: Support for feeds with audio URLs. Enjoy your favorite podcasts and audio content directly within Twine.
- **Reading Statistics**: Gain insights into your reading habits with a new statistics screen featuring reading heatmaps and top feed data.
- **Full-screen Image Viewer**: View images in articles in full screen with zoom and pan support.
- **Theme Variants**: Introduced multiple theme variants like Coral, Parchment, Forest, and more for a personalized look.
- **Unread Badges**: New unread indicators in the feed list, groups, and pinned sources.
- **Pinned Sources Improvements**: Support for drag-and-reorder in the pinned sources bar and a swipe-up gesture to open the navigation drawer.
- **Discovery**: A new feature to discover new feeds across various categories like technology, newsletters, and podcasts.
- **Readability Improvements**: Migration to a more robust `ReadabilityRunner` for better article extraction in reader view.
- **Full Article Sync**: Option to automatically download full article content during background sync (for FreshRSS and Miniflux).

## Changes

- **UI Refinement**: Standardized shapes for icons, improved layout and padding for post list items, and updated typography across the app.
- **Performance Optimizations**: Optimized database updates, memory usage during sync, and image loading for feed icons.
- **Navigation**: Migration to Compose Navigation for a smoother and more robust navigation experience.
- **Sync Enhancements**: Improved sync logic for FreshRSS and Miniflux, including bulk bookmarking and better group handling.
- **FOSS Variant**: Better support for FOSS builds by disabling proprietary components like Bugsnag.
- **Increased Free Feed Limit**: The number of free feeds has been increased from 15 to 30.

## Fixes

- Fixed an issue with blocked word triggers logic.
- Fixed crashes related to post read status updates and invalid post list keys.
- Improved favicon discovery when feed icons fail to load.
- Fixed various UI issues, including navigation bar padding and scroll behaviors.
- Resolved race conditions in the audio player on both Android and iOS.
