/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package dev.sasikanth.rss.reader.utils

import dev.sasikanth.rss.reader.data.utils.Constants

internal object Constants {
  const val EPSILON = 1e-6f
  const val REPORT_ISSUE_LINK = "https://github.com/msasikanth/twine/issues"

  const val ABOUT_SASI_NAME = "Sasikanth Miriyampalli"
  const val ABOUT_ED_NAME = "Eduardo Pratti"

  const val ABOUT_SASI_PIC =
    "https://www.gravatar.com/avatar/b03fa943d54ac5377813c44bfb8954d2?s=250"
  const val ABOUT_ED_PIC = "https://www.gravatar.com/avatar/b9fc51480a2bdf036e2e5f6e5a52bf19?s=250"

  const val ABOUT_SASI_THREADS = "https://www.threads.net/@its_sasikanth"
  const val ABOUT_SASI_TWITTER = "https://twitter.com/its_sasikanth"
  const val ABOUT_SASI_GITHUB = "https://github.com/msasikanth/"
  const val ABOUT_SASI_WEBSITE = "https://sasikanth.dev"
  const val ABOUT_ED_THREADS = "https://www.threads.net/@edpratti"
  const val ABOUT_ED_TWITTER = "https://twitter.com/edpratti"

  const val OPEN_SOURCE_LINK = "https://github.com/sponsors/msasikanth"

  const val DONT_KILL_MY_APP_LINK = "https://dontkillmyapp.com/"

  const val MINIMUM_REQUIRED_SEARCH_CHARACTERS = 3

  const val BADGE_COUNT_TRIM_LIMIT = 99

  const val ITEM_READ_ALPHA = 0.65f
  const val ITEM_UNREAD_ALPHA = 1f

  const val NUMBER_OF_FEATURED_POSTS = Constants.NUMBER_OF_FEATURED_POSTS
  const val IOS_APP_GROUP = Constants.APP_GROUP

  internal const val ENTITLEMENT_PREMIUM = "Premium"
}
