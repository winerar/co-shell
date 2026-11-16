package app.coshell.budgets.list.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The name of a limit and its «spent / limit» (#432): on one line when the amount leaves the name at least
 * [NAME_MIN_SHARE] of the width (or all it needs), the name cut with «…»; otherwise the amount goes whole to its own
 * line under the name, at the end. So a long amount never squeezes the name to nothing and is never cut itself.
 */
@Composable
internal fun NameAndAmount(name: @Composable () -> Unit, amount: @Composable () -> Unit, modifier: Modifier = Modifier) {
    Layout(contents = listOf(name, amount), modifier = modifier) { (nameSlot, amountSlot), constraints ->
        val nameItem = nameSlot.single()
        val amountItem = amountSlot.single()
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else Constraints.Infinity
        val gap = Spacing.s.roundToPx()
        val split = splitLine(
            width = width,
            nameNatural = nameItem.maxIntrinsicWidth(Constraints.Infinity),
            amountNatural = amountItem.maxIntrinsicWidth(Constraints.Infinity),
            gap = gap,
        )
        if (split != null) {
            val amountPlaced = amountItem.measure(Constraints(maxWidth = split.amount))
            val namePlaced = nameItem.measure(Constraints(maxWidth = split.name))
            val lineWidth = if (width == Constraints.Infinity) split.name + gap + amountPlaced.width else width
            val height = maxOf(namePlaced.height, amountPlaced.height)
            layout(lineWidth, height) {
                namePlaced.placeRelative(0, (height - namePlaced.height) / 2)
                amountPlaced.placeRelative(lineWidth - amountPlaced.width, (height - amountPlaced.height) / 2)
            }
        } else {
            val namePlaced = nameItem.measure(Constraints(maxWidth = width))
            val amountPlaced = amountItem.measure(Constraints(maxWidth = width))
            layout(width, namePlaced.height + amountPlaced.height) {
                namePlaced.placeRelative(0, 0)
                amountPlaced.placeRelative(width - amountPlaced.width, namePlaced.height)
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
