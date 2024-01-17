package dev.sasikanth.readability.util

open class RegExUtil(
  unlikelyCandidatesPattern: String = UNLIKELY_CANDIDATES_DEFAULT_PATTERN,
  okMaybeItsACandidatePattern: String = OK_MAYBE_ITS_A_CANDIDATE_DEFAULT_PATTERN,
  positivePattern: String = POSITIVE_DEFAULT_PATTERN,
  negativePattern: String = NEGATIVE_DEFAULT_PATTERN,
  extraneousPattern: String = EXTRANEOUS_DEFAULT_PATTERN,
  bylinePattern: String = BYLINE_DEFAULT_PATTERN,
  replaceFontsPattern: String = REPLACE_FONTS_DEFAULT_PATTERN,
  normalizePattern: String = NORMALIZE_DEFAULT_PATTERN,
  videosPattern: String = VIDEOS_DEFAULT_PATTERN,
  nextLinkPattern: String = NEXT_LINK_DEFAULT_PATTERN,
  prevLinkPattern: String = PREV_LINK_DEFAULT_PATTERN,
  whitespacePattern: String = WHITESPACE_DEFAULT_PATTERN,
  hasContentPattern: String = HAS_CONTENT_DEFAULT_PATTERN
) {

  companion object {
    const val UNLIKELY_CANDIDATES_DEFAULT_PATTERN =
      "banner|breadcrumbs|combx|comment|community|cover-wrap|disqus|extra|" +
        "foot|header|legends|menu|related|remark|replies|rss|shoutbox|sidebar|skyscraper|social|sponsor|supplemental|" +
        "ad-break|agegate|pagination|pager|popup|yom-remote"

    const val OK_MAYBE_ITS_A_CANDIDATE_DEFAULT_PATTERN = "and|article|body|column|main|shadow"

    const val POSITIVE_DEFAULT_PATTERN =
      "article|body|content|entry|hentry|h-entry|main|page|pagination|post|text|blog|story"

    const val NEGATIVE_DEFAULT_PATTERN =
      "hidden|^hid$| hid$| hid |^hid |banner|combx|comment|com-|contact|foot|footer|footnote|" +
        "masthead|media|meta|outbrain|promo|related|scroll|share|shoutbox|sidebar|skyscraper|sponsor|shopping|tags|tool|widget"

    const val EXTRANEOUS_DEFAULT_PATTERN =
      "print|archive|comment|discuss|e[\\-]?mail|share|reply|all|login|sign|single|utility"

    const val BYLINE_DEFAULT_PATTERN = "byline|author|dateline|writtenby|p-author"

    const val REPLACE_FONTS_DEFAULT_PATTERN = "<(/?)font[^>]*>"

    const val NORMALIZE_DEFAULT_PATTERN = "\\s{2,}"

    const val VIDEOS_DEFAULT_PATTERN =
      "//(www\\.)?(dailymotion|youtube|youtube-nocookie|player\\.vimeo)\\.com"

    const val NEXT_LINK_DEFAULT_PATTERN = "(next|weiter|continue|>([^\\|]|$)|»([^\\|]|$))"

    const val PREV_LINK_DEFAULT_PATTERN = "(prev|earl|old|new|<|«)"

    const val WHITESPACE_DEFAULT_PATTERN = "^\\s*$"

    const val HAS_CONTENT_DEFAULT_PATTERN = "\\S$"
  }

  protected val unlikelyCandidates: Regex

  protected val okMaybeItsACandidate: Regex

  protected val positive: Regex

  protected val negative: Regex

  protected val extraneous: Regex

  protected val byline: Regex

  protected val replaceFonts: Regex

  protected val normalize: Regex

  protected val videos: Regex

  protected val nextLink: Regex

  protected val prevLink: Regex

  protected val whitespace: Regex

  protected val hasContent: Regex

  init {
    this.unlikelyCandidates = Regex(unlikelyCandidatesPattern, RegexOption.IGNORE_CASE)
    this.okMaybeItsACandidate = Regex(okMaybeItsACandidatePattern, RegexOption.IGNORE_CASE)
    this.positive = Regex(positivePattern, RegexOption.IGNORE_CASE)
    this.negative = Regex(negativePattern, RegexOption.IGNORE_CASE)
    this.extraneous = Regex(extraneousPattern, RegexOption.IGNORE_CASE)
    this.byline = Regex(bylinePattern, RegexOption.IGNORE_CASE)
    this.replaceFonts = Regex(replaceFontsPattern, RegexOption.IGNORE_CASE)
    this.normalize = Regex(normalizePattern)
    this.videos = Regex(videosPattern, RegexOption.IGNORE_CASE)
    this.nextLink = Regex(nextLinkPattern, RegexOption.IGNORE_CASE)
    this.prevLink = Regex(prevLinkPattern, RegexOption.IGNORE_CASE)
    this.whitespace = Regex(whitespacePattern)
    this.hasContent = Regex(hasContentPattern)
  }

  open fun isPositive(matchString: String): Boolean {
    return positive.containsMatchIn(matchString)
  }

  open fun isNegative(matchString: String): Boolean {
    return negative.containsMatchIn(matchString)
  }

  open fun isUnlikelyCandidate(matchString: String): Boolean {
    return unlikelyCandidates.containsMatchIn(matchString)
  }

  open fun okMaybeItsACandidate(matchString: String): Boolean {
    return okMaybeItsACandidate.containsMatchIn(matchString)
  }

  open fun isByline(matchString: String): Boolean {
    return byline.containsMatchIn(matchString)
  }

  open fun hasContent(matchString: String): Boolean {
    return hasContent.containsMatchIn(matchString)
  }

  open fun isWhitespace(matchString: String): Boolean {
    return whitespace.containsMatchIn(matchString)
  }

  open fun normalize(text: String): String {
    return normalize.replace(text, " ")
  }

  open fun isVideo(matchString: String): Boolean {
    return videos.containsMatchIn(matchString)
  }
}
