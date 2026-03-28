## Features

- **Home Screen Widgets**: Added unread posts, bookmarks, unread count, and reading statistics widgets for Android and iOS. Includes support for different widget sizes (small, medium, large).
- **Home Screen Sort**: Introduced sort-aware post management, allowing you to view and navigate posts based on your preferred sort order.
- **Improved Feed Management**: Added the ability to delete feeds directly from the Group screen.
- **Deep Linking**: Enhanced app-wide deep link handling for better navigation from widgets and external sources.

## Changes

- **Improved Sync Performance**:
    - Implemented per-feed initial sync and robust offset-based pagination.
    - Reduced database load with improved indexes and queries.
- **UI & UX Refinements**:
    - Redesigned and stabilized home screen components for a smoother experience.
    - Refined navigation drawer and feed list selection styles with new animations.
    - Updated typography and UI consistency across the app.
    - Added tooltips to post action bar icons.

## Fixes

- Fixed widget navigation and layout issues on both Android and iOS.
- Improved empty state handling for widgets.
- Fixed an issue where posts could disappear when marked as read on scroll.
- Fixed JS evaluation in reader view on iOS.
- Fixed race conditions in the Audio Player across platforms.
