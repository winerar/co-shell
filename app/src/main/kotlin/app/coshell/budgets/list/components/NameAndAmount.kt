package app.coshell.budgets.list.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The name of a limit, its «spent / limit» and an optional [trailing] (the fold button) (#432): on one line when the
 * amount leaves the name at least [NAME_MIN_SHARE] of the width (or all it needs), the name cut with «…»; otherwise
 * the amount goes whole to its own line under the name, across the whole width (under the button too), at the end.
 * So a long amount never squeezes the name to nothing, never slides under the button and is never cut itself.
 */
@Composable
internal fun NameAndAmount(
    name: @Composable () -> Unit,
    amount: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Layout(contents = listOf(name, amount, trailing ?: {}), modifier = modifier) { (nameSlot, amountSlot, trailingSlot), constraints ->
        val nameItem = nameSlot.single()
        val amountItem = amountSlot.single()
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity
        val button = trailingSlot.firstOrNull()?.measure(Constraints())
        val buttonWidth = button?.width ?: 0
        val lineWidth = if (width == Constraints.Infinity) width else (width - buttonWidth).coerceAtLeast(0)
        val gap = Spacing.s.roundToPx()
        val split = splitLine(
            width = lineWidth,
            nameNatural = nameItem.maxIntrinsicWidth(Constraints.Infinity),
            amountNatural = amountItem.maxIntrinsicWidth(Constraints.Infinity),
            gap = gap,
        )
        val buttonHeight = button?.height ?: 0
        if (split != null) {
            val amountPlaced = amountItem.measure(Constraints(maxWidth = split.amount))
            val namePlaced = nameItem.measure(Constraints(maxWidth = split.name))
            val total = if (width == Constraints.Infinity) split.name + gap + amountPlaced.width + buttonWidth else width
            val height = maxOf(namePlaced.height, amountPlaced.height, buttonHeight)
            layout(total, height) {
                namePlaced.placeRelative(0, (height - namePlaced.height) / 2)
                amountPlaced.placeRelative(total - buttonWidth - amountPlaced.width, (height - amountPlaced.height) / 2)
                button?.placeRelative(total - buttonWidth, (height - buttonHeight) / 2)
            }
        } else {
            val namePlaced = nameItem.measure(Constraints(maxWidth = lineWidth))
            val amountPlaced = amountItem.measure(Constraints(maxWidth = width))
            val first = maxOf(namePlaced.height, buttonHeight)
            layout(width, first + amountPlaced.height) {
                namePlaced.placeRelative(0, (first - namePlaced.height) / 2)
                button?.placeRelative(width - buttonWidth, (first - buttonHeight) / 2)
                amountPlaced.placeRelative(width - amountPlaced.width, first)
            }
        }
    }
}

/** Widths of the name and the amount on one line, or `null` when the amount must go under the name. */
internal data class LineSplit(val name: Int, val amount: Int)

internal fun splitLine(width: Int, nameNatural: Int, amountNatural: Int, gap: Int): LineSplit? {
    if (width == Constraints.Infinity) return LineSplit(nameNatural, amountNatural)
    val nameNeeds = minOf(nameNatural, (width * NAME_MIN_SHARE).toInt())
    if (amountNatural + gap + nameNeeds > width) return null
    return LineSplit(name = width - amountNatural - gap, amount = amountNatural)
}

/** The name keeps at least this share of the line (or less, if that is all it needs). */
internal const val NAME_MIN_SHARE = 0.4f
