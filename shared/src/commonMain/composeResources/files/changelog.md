## Features

- **New Sync Services**: Added support for **Miniflux**, **FreshRSS** (Google Reader API), and **Dropbox** synchronization.
- **Navigation Drawer**: A completely redesigned navigation drawer for better organization and navigation across your feeds and groups.
- **Redesigned Settings**: Settings have been refactored into sub-screens with a fresh, modern UI.
- **Theme Variants**: Introduced multiple theme variants like Coral, Parchment, Forest, and more for a personalized look.
- **Audio Player**: Support for feeds with audio URLs, including a new **Sleep Timer**. Enjoy your favorite podcasts directly within Twine.
- **Reading Statistics**: Gain insights into your reading habits with a new statistics screen featuring reading heatmaps and top feed data.
- **Full-screen Image Viewer**: View images in articles in full screen with zoom and pan support.
- **Discovery**: A new feature to discover new feeds across various categories like technology, newsletters, subreddits, and podcasts.
- **Reading Time**: Articles now show an estimated reading time to help you plan your reading.
- **Search Enhancements**: New filters for bookmarked and unread posts in search results.

## Changes

- **Increased Free Feed Limit**: The number of free feeds has been increased from 15 to 30.
- **Typography & UI Refinement**:
    - Switched default app font to **Outfit** for better readability across the app.
    - Standardized shapes for icons, improved layout and padding for post list items.
- **Performance Optimizations**:
    - Optimized memory and bandwidth usage for favicon fetching and HTML parsing.
    - Optimized database updates and sync status push loops for better performance.
    - Reduced sync memory usage and improved initial sync window.
    - Optimized dynamic color updates to avoid unnecessary UI recompositions.
    - Enhanced `XmlFeedParser` performance by removing blocking operations.
    - Optimized shadow allocations and featured section for a smoother scrolling experience.
- **Sync Enhancements**: Improved sync logic for FreshRSS and Miniflux, including bulk bookmarking and better group handling.

## Fixes

- Fixed an issue with blocked word triggers logic.
- Fixed crashes related to post read status updates and invalid post list key decoding.
- Fixed JS evaluation in reader view on iOS and race conditions in the Audio Player across platforms.
- Improved favicon discovery when feed icons fail to load or are invalid.
- Improved sync reliability and handled foreign key constraints during feed deletion.
- Fixed various UI issues, including navigation bar padding, scroll behaviors, and iOS transitions.
