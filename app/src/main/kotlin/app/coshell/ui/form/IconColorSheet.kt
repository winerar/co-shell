package app.coshell.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.sheet.FormSheet

/**
 * «Иконка и цвет» (#290, board `Fb290Icons`): every icon and every colour in one sheet without scrolling; a tap
 * picks at once, «Готово» closes. [showIcons] off — colours only, [extra] under them (accounts: bank colours).
 */
@Composable
fun IconColorSheet(
    icon: String?,
    color: Long?,
    onIconSelect: (String) -> Unit,
    onColorSelect: (Long?) -> Unit,
    onClosed: () -> Unit,
    showIcons: Boolean = true,
    allowNone: Boolean = false,
    extra: (@Composable () -> Unit)? = null,
) {
    val title = stringResource(if (showIcons) R.string.form_icon_and_color else R.string.category_color)
    FormSheet(title, onClosed) {
        IconColorContent(icon, color, onIconSelect, onColorSelect, showIcons, allowNone)
        extra?.invoke()
    }
}

/** The body of [IconColorSheet] — on its own for the screenshot reference. */
@Composable
internal fun IconColorContent(
    icon: String?,
    color: Long?,
    onIconSelect: (String) -> Unit,
    onColorSelect: (Long?) -> Unit,
    showIcons: Boolean = true,
    allowNone: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        if (showIcons) {
            SheetCaption(stringResource(R.string.form_icons_count, CategoryIcons.keys.size))
            Grid(CategoryIcons.keys, ICON_COLUMNS, Spacing.s - Spacing.xxs) { key, cell ->
                IconCell(key, color, key == icon, cell.height(ICON_CELL)) { onIconSelect(key) }
            }
        }
        SheetCaption(stringResource(R.string.form_colors_count, CategoryPalette.colors.size))
        val swatch = color?.let(CategoryPalette::normalize)
        val items: List<Long?> = (if (allowNone) listOf(null) else emptyList<Long?>()) + CategoryPalette.colors
        Grid(items, COLOR_COLUMNS, Spacing.xs) { item, cell ->
            Row(cell, horizontalArrangement = Arrangement.Center) {
                if (item == null) NoColorDot(color == null) { onColorSelect(null) } else ColorDot(item, item == swatch) { onColorSelect(item) }
            }
        }
    }
}

@Composable
internal fun SheetCaption(text: String) =
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

/** Rows of [columns] equal cells; the last row keeps its cells the same width. */
@Composable
private fun <T> Grid(items: List<T>, columns: Int, gap: Dp, cell: @Composable (T, Modifier) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(gap)) {
        items.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                row.forEach { cell(it, Modifier.weight(1f)) }
                repeat(columns - row.size) { Row(Modifier.weight(1f)) {} }
            }
        }
    }
}

private const val ICON_COLUMNS = 8
private const val COLOR_COLUMNS = 10
private val ICON_CELL = Sizes.badgeSmall + Spacing.xs
