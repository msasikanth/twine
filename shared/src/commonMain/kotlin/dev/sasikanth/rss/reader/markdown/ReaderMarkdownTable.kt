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

package dev.sasikanth.rss.reader.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.unit.toSize
import com.mikepenz.markdown.annotator.AnnotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.LocalImageWidth
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.LocalMarkdownInlineContent
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.MarkdownDivider
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

private val MinTableCellWidth = 72.dp
private const val MinColumnChars = 8f
private const val MaxColumnChars = 28f
private val CompactTableCellWidth = 120.dp
private val CompactTableCellPadding = 8.dp
private val DefaultTableHorizontalPadding = 48.dp

internal val LocalReaderContentWidth = compositionLocalOf<() -> Dp> { { Dp.Unspecified } }

/**
 * The renderer's own table draws fixed width cells on a single ellipsized line and ignores the
 * column alignment. Article tables need all three: cells that wrap, columns that share the reader's
 * width when they fit in it, and the alignment the author set.
 */
@Composable
fun ReaderMarkdownTable(model: MarkdownComponentModel) {
  val columnCount =
    remember(model.node) {
      model.node.findChildOfType(GFMElementTypes.HEADER)?.children?.count {
        it.type == GFMTokenTypes.CELL
      } ?: 0
    }
  val rowsCount =
    remember(model.node) { model.node.children.count { it.type == GFMElementTypes.ROW } + 1 }
  val alignments = remember(model.node, model.content) { columnAlignments(model) }
  val weights =
    remember(model.node, model.content, columnCount) { columnWeights(model, columnCount) }
  val dimens = LocalMarkdownDimens.current
  val backgroundColor = LocalMarkdownColors.current.tableBackground
  val annotatorSettings = LocalMarkdownAnnotatorSettings.current ?: rememberAnnotatorSettings()
  val providedWidth = LocalReaderContentWidth.current()
  val density = LocalDensity.current
  val fallbackWidth =
    with(density) {
      (LocalWindowInfo.current.containerSize.width.toDp() - DefaultTableHorizontalPadding)
        .coerceAtLeast(0.dp)
    }
  val maxWidth =
    if (providedWidth != Dp.Unspecified && providedWidth > 0.dp) providedWidth else fallbackWidth

  val evenCellWidth = if (columnCount > 0) (maxWidth - 1.dp) / columnCount else Dp.Unspecified
  val cellWidth =
    if (columnCount > 0 && evenCellWidth >= MinTableCellWidth) evenCellWidth
    else dimens.tableCellWidth
  val cellPadding =
    if (cellWidth < CompactTableCellWidth) CompactTableCellPadding else dimens.tableCellPadding
  val tableWidth = columnCount * cellWidth
  val scrollable = maxWidth <= tableWidth
  val tableShape = remember(dimens.tableCornerSize) { RoundedCornerShape(dimens.tableCornerSize) }

  Box(
    modifier =
      Modifier.background(backgroundColor, tableShape)
        .widthIn(max = dimens.tableMaxWidth)
        .semantics {
          collectionInfo = CollectionInfo(rowCount = rowsCount, columnCount = columnCount)
        }
  ) {
    val columnModifier =
      if (scrollable) {
        Modifier.horizontalScroll(rememberScrollState()).requiredWidth(tableWidth)
      } else {
        Modifier.fillMaxWidth()
      }

    Column(modifier = columnModifier) {
      var rowIndex = 1
      model.node.children.forEach { child ->
        when (child.type) {
          GFMElementTypes.HEADER ->
            ReaderTableRow(
              content = model.content,
              row = child,
              tableWidth = tableWidth,
              style = model.typography.table,
              cellPadding = cellPadding,
              alignments = alignments,
              weights = weights,
              rowIndex = 0,
              annotatorSettings = annotatorSettings,
            )
          GFMElementTypes.ROW -> {
            ReaderTableRow(
              content = model.content,
              row = child,
              tableWidth = tableWidth,
              style = model.typography.table,
              cellPadding = cellPadding,
              alignments = alignments,
              weights = weights,
              rowIndex = rowIndex,
              annotatorSettings = annotatorSettings,
            )
            rowIndex++
          }
          GFMTokenTypes.TABLE_SEPARATOR -> MarkdownDivider()
        }
      }
    }
  }
}

@Composable
private fun ReaderTableRow(
  content: String,
  row: ASTNode,
  tableWidth: Dp,
  style: TextStyle,
  cellPadding: Dp,
  alignments: List<Alignment.Horizontal>,
  weights: List<Float>,
  rowIndex: Int,
  annotatorSettings: AnnotatorSettings,
) {
  val isHeader = rowIndex == 0
  val cellStyle =
    remember(style, isHeader) {
      style.copy(
        fontWeight = if (isHeader) FontWeight.Bold else style.fontWeight,
        hyphens = Hyphens.Auto,
      )
    }
  val cells = remember(row) { row.children.filter { it.type == GFMTokenTypes.CELL } }

  Row(verticalAlignment = Alignment.Top, modifier = Modifier.widthIn(tableWidth)) {
    cells.forEachIndexed { columnIndex, cell ->
      Column(
        modifier =
          Modifier.padding(cellPadding).weight(weights.getOrElse(columnIndex) { 1f }).semantics {
            if (isHeader) heading()
            collectionItemInfo =
              CollectionItemInfo(
                rowIndex = rowIndex,
                rowSpan = 1,
                columnIndex = columnIndex,
                columnSpan = 1,
              )
          },
        horizontalAlignment = alignments.getOrElse(columnIndex) { Alignment.Start },
      ) {
        SafeMarkdownTableCellText(
          content = content,
          cell = cell,
          style = cellStyle,
          annotatorSettings = annotatorSettings,
        )
      }
    }
  }
}

@Composable
private fun SafeMarkdownTableCellText(
  content: String,
  cell: ASTNode,
  style: TextStyle,
  annotatorSettings: AnnotatorSettings,
) {
  val text =
    remember(content, cell, style, annotatorSettings) {
      buildAnnotatedString {
        pushStyle(style.toSpanStyle())
        buildMarkdownAnnotatedString(
          content = content,
          node = cell,
          annotatorSettings = annotatorSettings,
        )
        pop()
      }
    }
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
  val userInlineContent = LocalMarkdownInlineContent.current.inlineContent
  val imageNodes = remember(cell) { collectImageNodes(cell) }

  if (imageNodes.isEmpty()) {
    BasicText(
      text = text,
      style = style,
      maxLines = Int.MAX_VALUE,
      overflow = TextOverflow.Clip,
      inlineContent = userInlineContent,
      color = colorProducer,
    )
    return
  }

  val transformer = LocalImageTransformer.current
  val inlineImageWidth = LocalImageWidth.current
  val density = LocalDensity.current
  val containerSize = remember { mutableStateOf(Size.Unspecified) }
  val imageSizeByLink = remember { mutableStateMapOf<String, Size>() }
  val resolvedInlineContent by
    remember(text, cell, userInlineContent, transformer, inlineImageWidth, density) {
      derivedStateOf {
        userInlineContent +
          buildImageInlineContent(
            content = text,
            node = cell,
            transformer = transformer,
            density = density,
            containerSize = containerSize.value,
            inlineImageWidth = inlineImageWidth,
            imageSizeByLink = imageSizeByLink,
            inlineImageAsBlock = false,
            imageSizeChanged = { link, size ->
              if (imageSizeByLink[link] != size) {
                imageSizeByLink[link] = size
              }
            },
          )
      }
    }

  BasicText(
    text = text,
    modifier =
      Modifier.onPlaced { coords ->
        if (!coords.isAttached) return@onPlaced
        coords.parentLayoutCoordinates?.also { parent ->
          if (parent.isAttached) {
            val nextSize = parent.size.toSize()
            if (containerSize.value != nextSize) {
              containerSize.value = nextSize
            }
          }
        }
      },
    style = style,
    maxLines = Int.MAX_VALUE,
    overflow = TextOverflow.Clip,
    inlineContent = resolvedInlineContent,
    color = colorProducer,
  )
}

/** Reads the column alignment out of the table's delimiter row, e.g. `| :--- | ---: |`. */
private fun columnAlignments(model: MarkdownComponentModel): List<Alignment.Horizontal> {
  val separator =
    model.node.children.firstOrNull { it.type == GFMTokenTypes.TABLE_SEPARATOR }
      ?: return emptyList()

  return separator.getTextInNode(model.content).toString().trim().trim('|').split('|').map { column
    ->
    val spec = column.trim()
    when {
      spec.startsWith(':') && spec.endsWith(':') -> Alignment.CenterHorizontally
      spec.endsWith(':') -> Alignment.End
      else -> Alignment.Start
    }
  }
}

/**
 * Columns share the table width in proportion to the longest cell in each, so a column of prose
 * does not end up as narrow as one holding a number. The weights sum to the column count, which
 * keeps the table the same overall width as evenly split columns.
 */
private fun columnWeights(model: MarkdownComponentModel, columnCount: Int): List<Float> {
  if (columnCount <= 0) return emptyList()

  val longest = FloatArray(columnCount)
  fun measure(row: ASTNode) {
    row.children
      .filter { it.type == GFMTokenTypes.CELL }
      .forEachIndexed { columnIndex, cell ->
        if (columnIndex < columnCount) {
          val text =
            cell.getTextInNode(model.content).toString().replace(LinkTargetRegex, "]").trim()
          longest[columnIndex] = maxOf(longest[columnIndex], text.length.toFloat())
        }
      }
  }

  model.node.findChildOfType(GFMElementTypes.HEADER)?.let(::measure)
  model.node.children.filter { it.type == GFMElementTypes.ROW }.forEach(::measure)

  val clamped = longest.map { it.coerceIn(MinColumnChars, MaxColumnChars) }
  val total = clamped.sum()
  return if (total <= 0f) List(columnCount) { 1f } else clamped.map { columnCount * it / total }
}

private val LinkTargetRegex = Regex("""\]\([^)]*\)""")
