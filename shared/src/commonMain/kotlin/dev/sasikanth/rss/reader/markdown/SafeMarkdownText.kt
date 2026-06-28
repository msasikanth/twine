/*
 * Copyright 2025 Twine Contributors
 *
 * This file is based on multiplatform-markdown-renderer's MarkdownText implementation.
 * The local copy guards LayoutCoordinates access during lazy/pager deactivation on iOS.
 */

package dev.sasikanth.rss.reader.markdown

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.toSize
import com.mikepenz.markdown.annotator.AnnotatorSettings
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.LocalImageWidth
import com.mikepenz.markdown.compose.LocalMarkdownAnimations
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.LocalMarkdownExtendedSpans
import com.mikepenz.markdown.compose.LocalMarkdownInlineContent
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.material.MarkdownBasicText
import com.mikepenz.markdown.compose.extendedspans.ExtendedSpans
import com.mikepenz.markdown.compose.extendedspans.drawBehind
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.ImageWidth
import com.mikepenz.markdown.model.MarkdownAnnotatorConfig
import com.mikepenz.markdown.utils.MARKDOWN_TAG_IMAGE_URL
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode

@Composable
fun SafeMarkdownText(
  content: String,
  node: ASTNode,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalMarkdownTypography.current.text,
) {
  SafeMarkdownText(AnnotatedString(content), node, modifier, style, sourceContent = content)
}

@Composable
fun SafeMarkdownText(
  content: String,
  node: ASTNode,
  style: TextStyle,
  modifier: Modifier = Modifier,
  contentChildType: IElementType? = null,
  annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
  val childNode = contentChildType?.run(node::findChildOfType) ?: node
  val styledText = buildAnnotatedString {
    pushStyle(style.toSpanStyle())
    buildMarkdownAnnotatedString(
      content = content,
      node = childNode,
      annotatorSettings = annotatorSettings,
    )
    pop()
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
  val baseColor = LocalMarkdownColors.current.text
  val animations = LocalMarkdownAnimations.current
  val transformer = LocalImageTransformer.current
  val inlineContent = LocalMarkdownInlineContent.current
  val inlineImageWidth = LocalImageWidth.current
  val density = LocalDensity.current
  val annotatorConfig = LocalMarkdownAnnotator.current.config

  val layoutResult: MutableState<TextLayoutResult?> = remember { mutableStateOf(null) }
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

  val inlineImageAsBlock = annotatorConfig.inlineImageAsBlock
  val imageNodes = remember(node) { collectImageNodes(node) }
  val resolved by
    remember(
      node,
      inlineContent.inlineContent,
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
          inlineContent.inlineContent +
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
              imageSizeChanged = { link, size -> imageSizeByLink += (link to size) },
            )
        map to blocks.sortedBy { it.start }
      }
    }
  val resolvedInlineContent = resolved.first
  val blockImageRanges = resolved.second

  val containerModifier: @Composable (Modifier) -> Modifier = { base ->
    base
      .semantics { isTraversalGroup = true }
      .onPlaced { coordinates -> containerSize.updateFromParent(coordinates) }
  }

  val textSegment: @Composable (AnnotatedString, Modifier) -> Unit = { segment, segmentModifier ->
    val extended =
      if (extendedSpans != null) remember(segment) { extendedSpans.extend(segment) } else segment
    val segmentDrawModifier =
      if (extendedSpans != null) segmentModifier.drawBehind(extendedSpans) else segmentModifier
    val hasSegmentLinks = segment.getLinkAnnotations(0, segment.length).isNotEmpty()
    val finalModifier =
      if (hasSegmentLinks) {
        segmentDrawModifier.semantics(mergeDescendants = true) {}
      } else {
        segmentDrawModifier
      }
    MarkdownBasicText(
      text = extended,
      modifier = finalModifier.let { animations.animateTextSize(it) },
      style = style,
      inlineContent = resolvedInlineContent,
      onTextLayout = { result ->
        layoutResult.value = result
        extendedSpans?.onTextLayout(result, baseColor)
        onTextLayout?.invoke(result, baseColor)
      },
    )
  }

  if (blockImageRanges.isEmpty()) {
    val hasLinks = content.getLinkAnnotations(0, content.length).isNotEmpty()
    if (hasLinks) {
      Box(modifier = containerModifier(modifier)) { textSegment(content, Modifier) }
    } else {
      textSegment(
        content,
        modifier.onPlaced { coordinates -> containerSize.updateFromParent(coordinates) },
      )
    }
  } else {
    val components = LocalMarkdownComponents.current
    val typography = LocalMarkdownTypography.current
    Column(modifier = containerModifier(modifier)) {
      var cursor = 0
      blockImageRanges.forEach { range ->
        if (range.start > cursor) {
          textSegment(content.subSequence(cursor, range.start), Modifier)
        }
        if (sourceContent != null && range.imageNode != null) {
          components.image(MarkdownComponentModel(sourceContent, range.imageNode, typography))
        } else {
          BlockFallbackImage(range.url)
        }
        cursor = range.end
      }
      if (cursor < content.length) {
        textSegment(content.subSequence(cursor, content.length), Modifier)
      }
    }
  }
}

@Composable
fun SafeMarkdownParagraph(
  content: String,
  node: ASTNode,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalMarkdownTypography.current.paragraph,
  annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
  val styledText = buildAnnotatedString {
    pushStyle(style.toSpanStyle())
    buildMarkdownAnnotatedString(
      content = content,
      node = node,
      annotatorSettings = annotatorSettings,
    )
    pop()
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
  val checked = node.getTextInNode(content).contains("[x]")
  Row { checkedIndicator(checked, Modifier.padding(end = 4.dp)) }
}

fun ASTNode.safeUnescapedTextInNode(content: String): String {
  return getUnescapedTextInNode(content)
}

private fun MutableState<Size>.updateFromParent(coordinates: LayoutCoordinates) {
  if (!coordinates.isAttached) return
  coordinates.parentLayoutCoordinates?.also { parentCoordinates ->
    if (parentCoordinates.isAttached) {
      value = parentCoordinates.size.toSize()
    }
  }
}

private fun collectImageNodes(root: ASTNode): List<ASTNode> {
  val list = mutableListOf<ASTNode>()

  fun visit(node: ASTNode) {
    if (node.type == MarkdownElementTypes.IMAGE) list += node
    node.children.forEach { visit(it) }
  }

  visit(root)
  return list
}

private data class BlockImageRange(
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

private fun buildImageInlineContent(
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
): Map<String, androidx.compose.foundation.text.InlineTextContent> {
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
        androidx.compose.foundation.text.InlineTextContent(
          Placeholder(
            width = with(density) { config.size.width.dp.toSp() },
            height = with(density) { config.size.height.dp.toSp() },
            placeholderVerticalAlign = config.verticalAlign,
          )
        ) { link ->
          MarkdownInlineImageWithSize(
            link = link,
            node = node,
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
  node: ASTNode,
  transformer: ImageTransformer,
  onSizeDetected: (Size) -> Unit,
) {
  val imageData = transformer.transform(link)
  val intrinsicSize = imageData?.let { transformer.intrinsicSize(it.painter) } ?: Size.Unspecified
  if (intrinsicSize != Size.Unspecified) {
    SideEffect { onSizeDetected(intrinsicSize) }
  }

  LocalMarkdownComponents.current.inlineImage(
    MarkdownComponentModel(link, node, LocalMarkdownTypography.current)
  )
}

private fun Density.toPxOrZero(unit: TextUnit, relativeToPx: Float = 0f): Float =
  when (unit.type) {
    TextUnitType.Sp -> unit.toPx()
    TextUnitType.Em -> unit.value * relativeToPx
    else -> 0f
  }
