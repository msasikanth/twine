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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.elements.LocalTableRowIndex
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.compose.elements.MarkdownTableBasicText
import com.mikepenz.markdown.model.markdownDimens
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

private val MinTableCellWidth = 72.dp
private val CompactTableCellWidth = 120.dp
private val CompactTableCellPadding = 8.dp

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
  val alignments = remember(model.node, model.content) { columnAlignments(model) }
  val dimens = LocalMarkdownDimens.current

  BoxWithConstraints {
    // Falling back to the fixed cell width leaves the table wider than the reader, which the
    // renderer then makes scrollable.
    val evenCellWidth = if (columnCount > 0) (maxWidth - 1.dp) / columnCount else Dp.Unspecified
    val cellWidth =
      if (columnCount > 0 && evenCellWidth >= MinTableCellWidth) evenCellWidth
      else dimens.tableCellWidth

    // Narrow columns spend too much of their width on padding, which pushes longer words
    // into mid-word breaks.
    val cellPadding =
      if (cellWidth < CompactTableCellWidth) CompactTableCellPadding else dimens.tableCellPadding

    CompositionLocalProvider(
      LocalMarkdownDimens provides
        markdownDimens(
          tableMaxWidth = dimens.tableMaxWidth,
          tableCellWidth = cellWidth,
          tableCellPadding = cellPadding,
          tableCornerSize = dimens.tableCornerSize,
        )
    ) {
      MarkdownTable(
        content = model.content,
        node = model.node,
        style = model.typography.table,
        headerBlock = { content, header, tableWidth, style ->
          ReaderTableRow(
            content = content,
            row = header,
            tableWidth = tableWidth,
            style = style,
            alignments = alignments,
            rowIndex = 0,
          )
        },
        rowBlock = { content, row, tableWidth, style ->
          ReaderTableRow(
            content = content,
            row = row,
            tableWidth = tableWidth,
            style = style,
            alignments = alignments,
            rowIndex = LocalTableRowIndex.current,
          )
        },
      )
    }
  }
}

@Composable
private fun ReaderTableRow(
  content: String,
  row: ASTNode,
  tableWidth: Dp,
  style: TextStyle,
  alignments: List<Alignment.Horizontal>,
  rowIndex: Int,
) {
  val isHeader = rowIndex == 0
  val cellPadding = LocalMarkdownDimens.current.tableCellPadding
  // Columns get narrow enough that a long word would otherwise break mid-word.
  val cellStyle =
    style.copy(
      fontWeight = if (isHeader) FontWeight.Bold else style.fontWeight,
      hyphens = Hyphens.Auto,
    )

  Row(verticalAlignment = Alignment.Top, modifier = Modifier.widthIn(tableWidth)) {
    row.children
      .filter { it.type == GFMTokenTypes.CELL }
      .forEachIndexed { columnIndex, cell ->
        Column(
          modifier =
            Modifier.padding(cellPadding).weight(1f).semantics {
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
          MarkdownTableBasicText(
            content = content,
            cell = cell,
            style = cellStyle,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip,
          )
        }
      }
  }
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
