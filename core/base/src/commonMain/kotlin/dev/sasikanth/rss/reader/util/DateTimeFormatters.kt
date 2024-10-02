/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.util

import kotlinx.datetime.Clock

internal val dateFormatterPatterns =
  setOf(
    // Keep the two character year before parsing the four
    // character year of similar pattern. Not sure why,
    // but unlike JVM, iOS is not keep it strict?
    "E, d MMM yy HH:mm:ss Z",
    "E, d MMM yyyy HH:mm:ss O",
    "E, d MMM yyyy HH:mm:ss Z",
    "E, d MMM yyyy HH:mm:ss z",
    "E, d MMM yyyy HH:mm Z",
    "E, dd MMM yyyy",
    "d MMM yyyy HH:mm:ss z",
    "yyyy-MM-dd'T'HH:mm:ssz",
    "yyyy-MM-dd'T'HH:mm:ssZ",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm:ss z",
    "yyyy-MM-dd",
    "MM-dd HH:mm:ss",
    "E, d MMM yyyy HH:mm:ss zzzz",
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
  )

expect fun String?.dateStringToEpochMillis(clock: Clock = Clock.System): Long?

data class DateTimeFormatException(val exception: Exception) : Exception()
