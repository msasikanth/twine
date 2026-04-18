## Features

- **Audio Progress Persistence**: Save and restore playback position for podcast/audio posts.

## Changes

- **Improved Sync Performance**:
    - Optimized database queries with new indexes for faster feed group and post lookups.
    - Batch update colors when using dynamic theming
- **Cloud Sync Status**: Refined sync status messaging to show relative time (e.g., "Synced • 2m ago") instead of static messages.
- **Reader Page**: Improved content spacing for better visual appearance.

## Fixes

- Fixed crash when no unread posts exist since last sync.
- Fixed iOS runtime crashes caused by paging library.
