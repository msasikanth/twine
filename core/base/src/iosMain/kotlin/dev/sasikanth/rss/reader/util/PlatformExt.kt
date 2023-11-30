/*
 * Copyright 2023 Sasikanth Miriyampalli
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

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSAttributedString
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.UIKit.NSCharacterEncodingDocumentAttribute
import platform.UIKit.NSDocumentTypeDocumentAttribute
import platform.UIKit.NSHTMLTextDocumentType
import platform.UIKit.create

actual val canBlurImage: Boolean = true

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Suppress("CAST_NEVER_SUCCEEDS")
actual fun String.decodeUrlEncodedString(): String {
  try {
    val data = (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
    if (data != null) {
      val decodedString =
        NSAttributedString.create(
            data = data,
            options =
              mapOf(
                NSDocumentTypeDocumentAttribute to NSHTMLTextDocumentType,
                NSCharacterEncodingDocumentAttribute to NSUTF8StringEncoding
              ),
            documentAttributes = null,
            error = null
          )
          ?.string

      if (decodedString != null) {
        return decodedString
      }
    }
  } catch (e: Exception) {
    // no-op
  }

  return this
}
