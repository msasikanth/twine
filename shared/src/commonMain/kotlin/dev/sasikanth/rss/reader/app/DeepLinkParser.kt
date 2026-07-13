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

package dev.sasikanth.rss.reader.app

import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import kotlinx.serialization.json.Json

object DeepLinkParser {

  fun parse(uriString: String): Screen? {
    if (uriString == "twine://add") {
      return Screen.AddFeed
    }
    if (uriString == "twine://bookmarks") {
      return Screen.Main(startTab = Screen.Main.TAB_BOOKMARKS)
    }
    if (uriString.startsWith("twine://reader/")) {
      try {
        val jsonStr = uriString.removePrefix("twine://reader/")
        // In Navigation 2, Reader arguments were passed as a JSON string in the route
        val args = Json.decodeFromString(ReaderScreenArgs.serializer(), jsonStr)
        return Screen.Reader(args)
      } catch (e: Exception) {
        // Fallback or ignore
      }
    }
    return null
  }
}
