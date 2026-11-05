## Features

- **New Add Feed Flow**: Adding a feed is now a two-step sheet. After entering a link, you can rename the feed, pick its icon, set up notifications, choose whether to always fetch the full article, and add it to groups.
- **Default Source on Startup**: Pick a feed or group to open automatically when the app starts. You can set this in behavior settings, and the picker includes search.
- **Feed List Filters**: New "Unread only" and "Hide grouped feeds" options in the feeds list help keep long lists tidy.
- **OPML Import During Onboarding**: Import your existing subscriptions straight from the onboarding screen.
- **Classic App Icon**: Added a new "Classic" app icon option.
- **Tables in Reader**: The reader now renders tables, with better column widths.
- **Keyboard Navigation**: Move through the post list with the keyboard, with shortcuts working anywhere in the window.

## Changes

- **Per-Feed Icons**: Choose between the feed's own icon and the website favicon for each feed. The global favicon setting now only sets the default for new feeds.
- **Smoother Home Navigation**: Added animated transitions when switching sources, plus a back button to return home from a selected source.
- **More Reliable Reader View**: Better full article extraction, with timeouts and a plain text fallback when parsing fails. Article content you already have is no longer replaced with shorter content.
- **Better Article Formatting**: Cleaner rendering of review scorecards and fewer empty headings on sites like The Verge.
- **Share Feedback on Desktop**: Desktop now shows a message when a link is copied to the clipboard instead of shared.
- **Refreshed Text Fields**: Text fields and login screens have a new look.
- **Performance Improvements**: Smoother UI and more efficient layouts.

## Fixes

- Fixed opened posts not always being marked as read.
- Fixed back press not clearing the selected source on the home screen.
- Fixed the reader jumping to the wrong post when the post list changed in the background.
- Fixed feeds failing to load when extra elements appeared before the channel tag.
- Fixed feeds served with less common XML content types not being recognized.
- Fixed the navigation drawer opening on swipe in Search, Discover, Bookmarks, and Settings.
