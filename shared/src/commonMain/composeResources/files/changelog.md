## Features

- **Split-Pane Layout**: On large screens, tablets, and desktop windows, browse your posts and read articles side-by-side in a new list-detail layout.
- **Google Drive & BazQux Sync**: Sync your feeds and articles with two new providers — Google Drive and BazQux — alongside existing FreshRSS, Miniflux, and Feedbin support.
- **New Theme Variants**: Choose from three new premium themes — Sepia, Slate, and Lavender — to further personalize your reading experience.
- **Confirm Mark All as Read**: Added an optional confirmation dialog before marking all articles as read, to help prevent accidental taps.

## Changes

- **Read Posts Stay Visible**: Articles you've opened no longer instantly disappear from the unread list while reading in split-pane view.
- **Smarter Reader Parsing**: Improved article parsing to avoid crashes on pages with embedded iframes, collapse duplicate headings, and better handle linked images.
- **Performance Improvements**: Optimized Home and Reader screen rendering, post pagination, image loading, and sync to make browsing and reading feel noticeably snappier.

## Fixes

- Fixed a crash in the article parser on pages containing iframe tags.
- Fixed active post tracking issues while paging through articles in the reader.
- Fixed audio playback to resume from the last position and handle cross-protocol redirects correctly.
- Fixed sign-out behavior when switching between sync service providers.
