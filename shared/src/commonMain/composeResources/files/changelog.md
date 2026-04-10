## Features

- **Audio Progress Persistence**: Save and restore playback position for podcast/audio posts. Progress is auto-saved every 10 seconds and when seeking, allowing you to resume playback from where you left off.

## Changes

- **Improved Sync Performance**:
    - Optimized database queries with new indexes for faster feed group and post lookups.
    - Batch update seed colors to eliminate N+1 query pattern, reducing SQLite transaction overhead.
- **Cloud Sync Status**: Refined sync status messaging to show relative time (e.g., "Synced • 2m ago") instead of static messages.
- **Reader Page**: Improved content spacing for better visual appearance.

## Fixes

- Fixed crash when no unread posts exist since last sync.
- Fixed iOS runtime crashes caused by paging library.
