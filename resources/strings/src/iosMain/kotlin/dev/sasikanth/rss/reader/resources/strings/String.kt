package dev.sasikanth.rss.reader.resources.strings

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

private val PATTERNS_REGEX = "%[\\d|.]*[sdf]".toRegex()

/**
 * copied from:
 * https://github.com/chrisbanes/tivi/blob/main/common/ui/resources/strings/src/iosMain/kotlin/app/tivi/common/ui/resources/String.kt#L16
 */
@Suppress("USELESS_CAST")
actual fun String.fmt(vararg args: Any?): String {
  val formats = PATTERNS_REGEX.findAll(this).map { it.groupValues.first() }.toList()

  var result = this

  formats.forEachIndexed { i, format ->
    val arg = args[i]

    val formatted =
      when (arg) {
        is Double -> NSString.stringWithFormat(format, arg as Double)
        is Float -> NSString.stringWithFormat(format, arg as Float)
        is Int -> NSString.stringWithFormat(format, arg as Int)
        is Long -> NSString.stringWithFormat(format, arg as Long)
        else -> NSString.stringWithFormat("%@", arg)
      }
    result = result.replaceFirst(format, formatted)
  }

  // We put the string through stringWithFormat again, to remove any escaped characters
  return NSString.stringWithFormat(result, Any())
}
