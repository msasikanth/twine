## Features

- **Feed Subscription Health**: Easily keep track of your feeds. Twine now monitors and shows health statistics for your feed subscriptions, helping you identify and manage failing or inactive sources.
- **Collapsible Feed Groups**: Keep your feed discovery screen organized by collapsing and expanding feed groups as needed.

## Changes

- **iOS Notifications**: Removed notification settings and disabled support for notifications on iOS.
- **Sleeker Discovery**: The Discovery screen has been redesigned into a clean flat list with sticky headers, making browsing much smoother.
- **Refined Navigation Drawer**: Improved UI layouts, adjusted control visibility, and added clean dividers inside the navigation drawer.
- **Better Feed Compatibility**: Feed requests now include standard HTTP Accept headers to ensure successful updates from feeds with stricter retrieval rules.
- **Optimized Performance**: Significant under-the-hood composition optimizations on the home screen, layout calculations in the featured section, and shadow rendering on the reader screen.

## Fixes

- Fixed a race condition that could cause duplicate notifications during background feed synchronizations.
- Fixed a background crash on iOS.
- Fixed home screen scroll state persistence bugs to ensure the article list remains stable when returning to it.
