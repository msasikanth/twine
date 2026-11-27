/*
 * Copyright 2025 Twine Contributors
 *
 * This file is based on multiplatform-markdown-renderer's MarkdownText implementation.
 * The local copy guards LayoutCoordinates access during lazy/pager deactivation on iOS.
 */

package dev.sasikanth.rss.reader.markdown

import androidx.collection.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.toSize
import com.mikepenz.markdown.annotator.AnnotatorSettings
import com.mikepenz.markdown.annotator.DefaultAnnotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.LocalImageWidth
import com.mikepenz.markdown.compose.LocalMarkdownAnimations
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.LocalMarkdownExtendedSpans
import com.mikepenz.markdown.compose.LocalMarkdownInlineContent
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.LocalReferenceLinkHandler
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.MarkdownCodeBackground
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans
import com.mikepenz.markdown.compose.extendedspans.drawBehind
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.ImageWidth
import com.mikepenz.markdown.model.MarkdownAnnotator
import com.mikepenz.markdown.model.MarkdownAnnotatorConfig
import com.mikepenz.markdown.model.ReferenceLinkHandler
import com.mikepenz.markdown.utils.MARKDOWN_TAG_IMAGE_URL
import com.mikepenz.markdown.utils.codeSpanStyle
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import dev.sasikanth.rss.reader.ui.LocalImageSharedElementScopeKey
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.BoldHighlight
import dev.snipme.highlights.model.ColorHighlight
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

internal val LocalMarkdownAnnotatorSettings = compositionLocalOf<AnnotatorSettings?> { null }
internal val LocalMarkdownTextColorProducer = compositionLocalOf<ColorProducer?> { null }

private data class HighlightedCodeCacheKey(
  val code: String,
  val language: String?,
  val isDarkTheme: Boolean,
)

private val HighlightedCodeCache = LruCache<HighlightedCodeCacheKey, AnnotatedString>(maxSize = 32)
private val AtomLightSyntaxTheme = SyntaxThemes.atom(darkMode = false)
private val AtomDarkSyntaxTheme = SyntaxThemes.atom(darkMode = true)

@Composable
internal fun rememberAnnotatorSettings(
  linkTextSpanStyle: TextLinkStyles = LocalMarkdownTypography.current.textLink,
  codeSpanStyle: SpanStyle = LocalMarkdownTypography.current.codeSpanStyle,
  annotator: MarkdownAnnotator = LocalMarkdownAnnotator.current,
  referenceLinkHandler: ReferenceLinkHandler? = LocalReferenceLinkHandler.current,
  uriHandler: UriHandler = LocalUriHandler.current,
  highlightSpanStyle: SpanStyle =
    LocalMarkdownColors.current.let {
      SpanStyle(color = it.highlightText, background = it.highlightBackground)
    },
): AnnotatorSettings {
  return remember(
    linkTextSpanStyle,
    codeSpanStyle,
    annotator,
    referenceLinkHandler,
    uriHandler,
    highlightSpanStyle,
  ) {
    DefaultAnnotatorSettings(
      linkTextSpanStyle = linkTextSpanStyle,
      codeSpanStyle = codeSpanStyle,
      annotator = annotator,
      referenceLinkHandler = referenceLinkHandler,
      linkInteractionListener = { link ->
        val annotationUrl = (link as? LinkAnnotation.Url)?.url
        if (annotationUrl != null) {
          val foundReference =
            referenceLinkHandler?.find(annotationUrl)?.takeIf { it.isNotEmpty() } ?: annotationUrl
          try {
            uriHandler.openUri(foundReference)
          } catch (t: Throwable) {
            println("Could not open the provided url: $foundReference // ${t.message}")
          }
        }
      },
      highlightSpanStyle = highlightSpanStyle,
    )
  }
}

@Composable
fun SafeMarkdownText(
  content: String,
  node: ASTNode,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalMarkdownTypography.current.text,
) {
  val annotated = remember(content) { AnnotatedString(content) }
  SafeMarkdownText(annotated, node, modifier, style, sourceContent = content)
}

@Composable
fun SafeMarkdownText(
  content: String,
  node: ASTNode,
  style: TextStyle,
  modifier: Modifier = Modifier,
  contentChildType: IElementType? = null,
  annotatorSettings: AnnotatorSettings =
    LocalMarkdownAnnotatorSettings.current ?: rememberAnnotatorSettings(),
) {
  val childNode =
    remember(node, contentChildType) { contentChildType?.run(node::findChildOfType) ?: node }
  val styledText =
    remember(content, childNode, style, annotatorSettings) {
      buildAnnotatedString {
        pushStyle(style.toSpanStyle())
        buildMarkdownAnnotatedString(
          content = content,
          node = childNode,
          annotatorSettings = annotatorSettings,
        )
        pop()
      }
    }

  SafeMarkdownText(
    content = styledText,
    node = node,
    modifier = modifier,
    style = style,
    sourceContent = content,
  )
}

@Composable
fun SafeMarkdownText(
  content: AnnotatedString,
  node: ASTNode,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalMarkdownTypography.current.text,
  extendedSpans: ExtendedSpans? = LocalMarkdownExtendedSpans.current.extendedSpans?.invoke(),
  sourceContent: String? = null,
) {
  SafeMarkdownText(
    content = content,
    node = node,
    modifier = modifier,
    style = style,
    onTextLayout = null,
    sourceContent = sourceContent,
    extendedSpans = extendedSpans,
  )
}

@Composable
fun SafeMarkdownText(
  content: AnnotatedString,
  node: ASTNode,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalMarkdownTypography.current.text,
  onTextLayout: ((TextLayoutResult, Color?) -> Unit)?,
  sourceContent: String? = null,
  extendedSpans: ExtendedSpans? = null,
) {
  val imageNodes = remember(node) { collectImageNodes(node) }
  val inlineContent = LocalMarkdownInlineContent.current.inlineContent

  if (imageNodes.isEmpty()) {
    val hasLinks = remember(content) { content.getLinkAnnotations(0, content.length).isNotEmpty() }
    if (hasLinks) {
      Box(modifier = modifier.semantics { isTraversalGroup = true }) {
        SafeMarkdownTextSegment(
          segment = content,
          modifier = Modifier,
          style = style,
          inlineContent = inlineContent,
          extendedSpans = extendedSpans,
          onTextLayout = onTextLayout,
          hasLinks = true,
        )
      }
    } else {
      SafeMarkdownTextSegment(
        segment = content,
        modifier = modifier,
        style = style,
        inlineContent = inlineContent,
        extendedSpans = extendedSpans,
        onTextLayout = onTextLayout,
        hasLinks = false,
      )
    }
    return
  }

  SafeMarkdownTextWithInlineImages(
    content = content,
    node = node,
    imageNodes = imageNodes,
    userInlineContent = inlineContent,
    modifier = modifier,
    style = style,
    onTextLayout = onTextLayout,
    sourceContent = sourceContent,
    extendedSpans = extendedSpans,
  )
}

@Composable
private fun SafeMarkdownTextWithInlineImages(
  content: AnnotatedString,
  node: ASTNode,
  imageNodes: List<ASTNode>,
  userInlineContent: Map<String, InlineTextContent>,
  modifier: Modifier,
  style: TextStyle,
  onTextLayout: ((TextLayoutResult, Color?) -> Unit)?,
  sourceContent: String?,
  extendedSpans: ExtendedSpans?,
) {
  val transformer = LocalImageTransformer.current
  val inlineImageWidth = LocalImageWidth.current
  val density = LocalDensity.current
  val inlineImageAsBlock = LocalMarkdownAnnotator.current.config.inlineImageAsBlock

  val containerSize = remember { mutableStateOf(Size.Unspecified) }
  val imageSizeByLink = remember { mutableStateMapOf<String, Size>() }

  val lineHeightPx =
    with(density) {
      val fontSizePx = toPxOrZero(style.fontSize, relativeToPx = 0f)
      when {
        style.lineHeight.isSpecified -> toPxOrZero(style.lineHeight, relativeToPx = fontSizePx)
        else -> fontSizePx
      }
    }

  val resolved by
    remember(
      node,
      userInlineContent,
      content,
      transformer,
      inlineImageWidth,
      lineHeightPx,
      inlineImageAsBlock,
      imageNodes,
      density,
    ) {
      derivedStateOf {
        val blocks = mutableListOf<BlockImageRange>()
        val map =
          userInlineContent +
            buildImageInlineContent(
              content = content,
              node = node,
              transformer = transformer,
              density = density,
              containerSize = containerSize.value,
              inlineImageWidth = inlineImageWidth,
              imageSizeByLink = imageSizeByLink,
              lineHeightPx = lineHeightPx,
              inlineImageAsBlock = inlineImageAsBlock,
              imageNodes = imageNodes,
              onBlockImage = { range -> blocks += range },
              imageSizeChanged = { link, size ->
                if (imageSizeByLink[link] != size) {
                  imageSizeByLink[link] = size
                }
              },
            )
        map to blocks.sortedBy { it.start }
      }
    }
  val resolvedInlineContent = resolved.first
  val blockImageRanges = resolved.second

  if (blockImageRanges.isEmpty()) {
    val hasLinks = remember(content) { content.getLinkAnnotations(0, content.length).isNotEmpty() }
    if (hasLinks) {
      Box(
        modifier =
          modifier
            .semantics { isTraversalGroup = true }
            .onPlaced { coordinates -> containerSize.updateFromParent(coordinates) }
      ) {
        SafeMarkdownTextSegment(
          segment = content,
          modifier = Modifier,
          style = style,
          inlineContent = resolvedInlineContent,
          extendedSpans = extendedSpans,
          onTextLayout = onTextLayout,
          hasLinks = true,
        )
      }
    } else {
      SafeMarkdownTextSegment(
        segment = content,
        modifier = modifier.onPlaced { coordinates -> containerSize.updateFromParent(coordinates) },
        style = style,
        inlineContent = resolvedInlineContent,
        extendedSpans = extendedSpans,
        onTextLayout = onTextLayout,
        hasLinks = false,
      )
    }
  } else {
    val components = LocalMarkdownComponents.current
    val typography = LocalMarkdownTypography.current
    Column(
      modifier =
        modifier
          .semantics { isTraversalGroup = true }
          .onPlaced { coordinates -> containerSize.updateFromParent(coordinates) }
    ) {
      var cursor = 0
      blockImageRanges.forEach { range ->
        if (range.start > cursor) {
          val segment = content.subSequence(cursor, range.start)
          SafeMarkdownTextSegment(
            segment = segment,
            modifier = Modifier,
            style = style,
            inlineContent = resolvedInlineContent,
            extendedSpans = extendedSpans,
            onTextLayout = onTextLayout,
            hasLinks = null,
          )
        }
        CompositionLocalProvider(
          LocalImageSharedElementScopeKey provides
            "${LocalImageSharedElementScopeKey.current}:${range.start}"
        ) {
          if (sourceContent != null && range.imageNode != null) {
            components.image(MarkdownComponentModel(sourceContent, range.imageNode, typography))
          } else {
            BlockFallbackImage(range.url)
          }
        }
        cursor = range.end
      }
      if (cursor < content.length) {
        val segment = content.subSequence(cursor, content.length)
        SafeMarkdownTextSegment(
          segment = segment,
          modifier = Modifier,
          style = style,
          inlineContent = resolvedInlineContent,
          extendedSpans = extendedSpans,
          onTextLayout = onTextLayout,
          hasLinks = null,
        )
      }
    }
  }
}

@Composable
private fun SafeMarkdownTextSegment(
  segment: AnnotatedString,
  modifier: Modifier,
  style: TextStyle,
  inlineContent: Map<String, InlineTextContent>,
  extendedSpans: ExtendedSpans?,
  onTextLayout: ((TextLayoutResult, Color?) -> Unit)?,
  hasLinks: Boolean?,
) {
  val animations = LocalMarkdownAnimations.current
  val textColorProducer = LocalMarkdownTextColorProducer.current
  val fallbackColor = LocalMarkdownColors.current.text
  val colorProducer =
    remember(style, textColorProducer, fallbackColor) {
      ColorProducer {
        when {
          style.color.isSpecified -> style.color
          textColorProducer != null -> textColorProducer()
          else -> fallbackColor
        }
      }
    }

  val extended =
    if (extendedSpans != null) remember(segment, extendedSpans) { extendedSpans.extend(segment) }
    else segment
  val segmentDrawModifier =
    if (extendedSpans != null) modifier.drawBehind(extendedSpans) else modifier
  val hasSegmentLinks =
    hasLinks ?: remember(segment) { segment.getLinkAnnotations(0, segment.length).isNotEmpty() }
  val finalModifier =
    if (hasSegmentLinks) {
      segmentDrawModifier.semantics(mergeDescendants = true) {}
    } else {
      segmentDrawModifier
    }

  val textLayoutCallback: ((TextLayoutResult) -> Unit)? =
    if (extendedSpans != null || onTextLayout != null) {
      { result ->
        extendedSpans?.onTextLayout(result, fallbackColor)
        onTextLayout?.invoke(result, fallbackColor)
      }
    } else {
      null
    }

  BasicText(
    text = extended,
    modifier = animations.animateTextSize(finalModifier),
    style = style,
    inlineContent = inlineContent,
    onTextLayout = textLayoutCallback,
    color = colorProducer,
  )
}

@Composable
internal fun SafeMarkdownHighlightedCodeFence(
  content: String,
  node: ASTNode,
  isDarkTheme: Boolean,
  showHeader: Boolean = true,
  style: TextStyle = LocalMarkdownTypography.current.code,
) {
  MarkdownCodeFence(content, node, style) { code, language, codeStyle ->
    SafeMarkdownHighlightedCode(
      code = code,
      language = language,
      style = codeStyle,
      isDarkTheme = isDarkTheme,
      showHeader = showHeader,
    )
  }
}

@Composable
internal fun SafeMarkdownHighlightedCodeBlock(
  content: String,
  node: ASTNode,
  isDarkTheme: Boolean,
  showHeader: Boolean = true,
  style: TextStyle = LocalMarkdownTypography.current.code,
) {
  MarkdownCodeBlock(content, node, style) { code, language, codeStyle ->
    SafeMarkdownHighlightedCode(
      code = code,
      language = language,
      style = codeStyle,
      isDarkTheme = isDarkTheme,
      showHeader = showHeader,
    )
  }
}

@Composable
private fun SafeMarkdownHighlightedCode(
  code: String,
  language: String?,
  style: TextStyle,
  isDarkTheme: Boolean,
  showHeader: Boolean,
) {
  val backgroundCodeColor = LocalMarkdownColors.current.codeBackground
  val codeBackgroundCornerSize = LocalMarkdownDimens.current.codeBackgroundCornerSize
  val codeBlockPadding = LocalMarkdownPadding.current.codeBlock
  val textColorProducer = LocalMarkdownTextColorProducer.current
  val fallbackColor = LocalMarkdownColors.current.text
  val colorProducer =
    remember(style, textColorProducer, fallbackColor) {
      ColorProducer {
        when {
          style.color.isSpecified -> style.color
          textColorProducer != null -> textColorProducer()
          else -> fallbackColor
        }
      }
    }

  val cacheKey =
    remember(code, language, isDarkTheme) {
      HighlightedCodeCacheKey(code = code, language = language, isDarkTheme = isDarkTheme)
    }
  val cachedHighlights = remember(cacheKey) { HighlightedCodeCache[cacheKey] }
  val codeHighlights: AnnotatedString =
    if (cachedHighlights != null) {
      cachedHighlights
    } else {
      val produced by
        produceState(initialValue = AnnotatedString(text = code), key1 = cacheKey) {
          val highlighted =
            withContext(Dispatchers.Default) {
              buildHighlightedAnnotatedString(
                code = code,
                language = language,
                isDarkTheme = isDarkTheme,
              )
            }
          HighlightedCodeCache.put(cacheKey, highlighted)
          value = highlighted
        }
      produced
    }

  val codeShape =
    remember(codeBackgroundCornerSize) { RoundedCornerShape(codeBackgroundCornerSize) }
  MarkdownCodeBackground(
    color = backgroundCodeColor,
    shape = codeShape,
    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    showHeader = showHeader,
    language = language,
    code = code,
  ) {
    BasicText(
      text = codeHighlights,
      style = style,
      color = colorProducer,
      modifier = Modifier.horizontalScroll(rememberScrollState()).padding(codeBlockPadding),
    )
  }
}

private fun buildHighlightedAnnotatedString(
  code: String,
  language: String?,
  isDarkTheme: Boolean,
): AnnotatedString {
  val syntaxTheme = if (isDarkTheme) AtomDarkSyntaxTheme else AtomLightSyntaxTheme
  val syntaxLanguage = language?.let { SyntaxLanguage.getByName(it) }
  val builder = Highlights.Builder().theme(syntaxTheme).code(code)
  if (syntaxLanguage != null) {
    builder.language(syntaxLanguage)
  }
  val codeHighlights = builder.build().getHighlights()
  return buildAnnotatedString {
    append(code)
    codeHighlights.forEach {
      val spanStyle =
        when (it) {
          is ColorHighlight -> SpanStyle(color = Color(it.rgb).copy(alpha = 1f))
          is BoldHighlight -> SpanStyle(fontWeight = FontWeight.Bold)
        }
      addStyle(style = spanStyle, start = it.location.start, end = it.location.end)
    }
  }
}

@Composable
fun SafeMarkdownParagraph(
  content: String,
  node: ASTNode,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalMarkdownTypography.current.paragraph,
  annotatorSettings: AnnotatorSettings =
    LocalMarkdownAnnotatorSettings.current ?: rememberAnnotatorSettings(),
) {
  val styledText =
    remember(content, node, style, annotatorSettings) {
      buildAnnotatedString {
        pushStyle(style.toSpanStyle())
        buildMarkdownAnnotatedString(
          content = content,
          node = node,
          annotatorSettings = annotatorSettings,
        )
        pop()
      }
    }

  SafeMarkdownText(
    content = styledText,
    node = node,
    modifier = modifier,
    style = style,
    sourceContent = content,
  )
}

@Composable
fun SafeMarkdownHeader(
  content: String,
  node: ASTNode,
  style: TextStyle,
  contentChildType: IElementType = MarkdownTokenTypes.ATX_CONTENT,
) =
  SafeMarkdownText(
    modifier = Modifier.semantics { heading() },
    content = content,
    node = node,
    style = style,
    contentChildType = contentChildType,
  )

@Composable
fun SafeMarkdownCheckBox(
  content: String,
  node: ASTNode,
  style: TextStyle,
  checkedIndicator: @Composable (Boolean, Modifier) -> Unit = { checked, modifier ->
    SafeMarkdownText(
      content = "[${if (checked) "x" else " "}] ",
      node = node,
      modifier = modifier,
      style = style.copy(fontFamily = FontFamily.Monospace),
    )
  },
) {
  val checked = remember(node, content) { node.getTextInNode(content).contains("[x]") }
  Row { checkedIndicator(checked, Modifier.padding(end = 4.dp)) }
}

fun ASTNode.safeUnescapedTextInNode(content: String): String {
  return getUnescapedTextInNode(content)
}

private fun MutableState<Size>.updateFromParent(coordinates: LayoutCoordinates) {
  if (!coordinates.isAttached) return
  coordinates.parentLayoutCoordinates?.also { parentCoordinates ->
    if (parentCoordinates.isAttached) {
      val nextSize = parentCoordinates.size.toSize()
      if (value != nextSize) {
        value = nextSize
      }
    }
  }
}

internal fun collectImageNodes(root: ASTNode): List<ASTNode> {
  val list = mutableListOf<ASTNode>()

  fun visit(node: ASTNode) {
    if (node.type == MarkdownElementTypes.IMAGE) list += node
    node.children.forEach { visit(it) }
  }

  visit(root)
  return list
}

internal data class BlockImageRange(
  val url: String,
  val start: Int,
  val end: Int,
  val imageNode: ASTNode?,
)

@Composable
private fun BlockFallbackImage(url: String) {
  LocalImageTransformer.current.transform(url)?.let { imageData ->
    Image(
      painter = imageData.painter,
      contentDescription = imageData.contentDescription,
      modifier = imageData.modifier,
      alignment = imageData.alignment,
      contentScale = imageData.contentScale,
      alpha = imageData.alpha,
      colorFilter = imageData.colorFilter,
    )
  }
}

internal fun buildImageInlineContent(
  content: AnnotatedString,
  node: ASTNode,
  transformer: ImageTransformer,
  density: Density,
  containerSize: Size,
  inlineImageWidth: ImageWidth,
  imageSizeByLink: Map<String, Size>,
  defaultImageSize: Size = Size.Unspecified,
  lineHeightPx: Float = 0f,
  inlineImageAsBlock: Boolean = true,
  imageNodes: List<ASTNode> = emptyList(),
  onBlockImage: ((BlockImageRange) -> Unit)? = null,
  imageSizeChanged: ((link: String, Size) -> Unit)? = null,
): Map<String, InlineTextContent> {
  val annotations =
    content
      .getStringAnnotations(0, content.length)
      .filter { it.item.startsWith("${MARKDOWN_TAG_IMAGE_URL}_") }
      .sortedBy { it.start }

  fun shouldPromote(url: String): Boolean {
    val imageSize = imageSizeByLink[url] ?: defaultImageSize
    return inlineImageAsBlock &&
      lineHeightPx > 0f &&
      !imageSize.isUnspecified &&
      imageSize.height > lineHeightPx * MarkdownAnnotatorConfig.BLOCK_FALLBACK_LINE_MULTIPLIER
  }

  annotations.forEachIndexed { index, annotation ->
    val url = annotation.item.removePrefix("${MARKDOWN_TAG_IMAGE_URL}_")
    if (shouldPromote(url)) {
      onBlockImage?.invoke(
        BlockImageRange(
          url = url,
          start = annotation.start,
          end = annotation.end,
          imageNode = imageNodes.getOrNull(index),
        )
      )
    }
  }

  return annotations
    .groupBy { it.item }
    .mapNotNull { (tag, _) ->
      val url = tag.removePrefix("${MARKDOWN_TAG_IMAGE_URL}_")
      if (shouldPromote(url)) return@mapNotNull null

      val imageSize = imageSizeByLink[url] ?: defaultImageSize
      val config =
        transformer.placeholderConfig(
          url,
          density,
          containerSize,
          inlineImageWidth,
          imageSize,
          imageSizeChanged,
        )

      tag to
        InlineTextContent(
          Placeholder(
            width = with(density) { config.size.width.dp.toSp() },
            height = with(density) { config.size.height.dp.toSp() },
            placeholderVerticalAlign = config.verticalAlign,
          )
        ) { link ->
          MarkdownInlineImageWithSize(
            link = link,
            transformer = transformer,
            onSizeDetected = { detectedSize -> imageSizeChanged?.invoke(url, detectedSize) },
          )
        }
    }
    .toMap()
}

@Composable
private fun MarkdownInlineImageWithSize(
  link: String,
  transformer: ImageTransformer,
  onSizeDetected: (Size) -> Unit,
) {
  val imageData = transformer.transform(link) ?: return
  val intrinsicSize = transformer.intrinsicSize(imageData.painter)
  if (intrinsicSize != Size.Unspecified) {
    SideEffect(intrinsicSize) { onSizeDetected(intrinsicSize) }
  }

  Image(
    painter = imageData.painter,
    contentDescription = imageData.contentDescription,
    modifier = Modifier.fillMaxSize().then(imageData.modifier),
    alignment = imageData.alignment,
    contentScale = imageData.contentScale,
    alpha = imageData.alpha,
    colorFilter = imageData.colorFilter,
  )
}

private fun Density.toPxOrZero(unit: TextUnit, relativeToPx: Float = 0f): Float =
  when (unit.type) {
    TextUnitType.Sp -> unit.toPx()
    TextUnitType.Em -> unit.value * relativeToPx
    else -> 0f
  }
