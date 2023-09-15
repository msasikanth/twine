package dev.sasikanth.rss.reader.resources.strings

actual fun String.fmt(vararg args: Any?): String = format(*args)
