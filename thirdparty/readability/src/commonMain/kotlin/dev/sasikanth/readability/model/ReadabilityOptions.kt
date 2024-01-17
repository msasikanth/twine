package dev.sasikanth.readability.model

open class ReadabilityOptions(
  val maxElemsToParse: Int = DEFAULT_MAX_ELEMS_TO_PARSE,
  val nbTopCandidates: Int = DEFAULT_N_TOP_CANDIDATES,
  val wordThreshold: Int = DEFAULT_WORD_THRESHOLD,
  val additionalClassesToPreserve: List<String> = emptyList()
) {

  companion object {
    // Max number of nodes supported by this parser. Default: 0 (no limit)
    const val DEFAULT_MAX_ELEMS_TO_PARSE = 0

    // The number of top candidates to consider when analysing how
    // tight the competition is among candidates.
    const val DEFAULT_N_TOP_CANDIDATES = 5

    // The default number of words an article must have in order to return a result
    const val DEFAULT_WORD_THRESHOLD = 500
  }
}
