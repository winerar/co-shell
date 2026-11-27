package app.coshell.core.designsystem.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Widths of the bottom bar's cells (#796, board `MainBottomBar`, `nav_columns` in `mockup_bricks.py`): the «+» keeps its slot in the
 * middle; the selected tab takes its half of the bar minus a [tabMin]-wide neighbour, the other half stays equal. Pure maths, tested
 * without Compose (`BarWidthsTest`).
 */
internal object BarWidths {
    /** The «+» slot: a tab's share of the bar, but never above [addSlotMax]. */
    fun addSlot(inner: Dp, tabs: Int, addSlotMax: Dp): Dp = minOf(addSlotMax, inner / (tabs + 1))

    /**
     * Width of every tab of [tabs] for a bar [inner] wide with the «+» between the halves; [selected] — index of the chosen tab or −1.
     * A half with the selected tab: it gets the room left by its neighbours, which shrink to [tabMin] (never narrower than equal share, so
     * a narrow bar does not squeeze anything below a touch target).
     */
    fun of(tabs: Int, selected: Int, inner: Dp, addSlot: Dp, tabMin: Dp): List<Dp> {
        if (tabs == 0) return emptyList()
        val leftCount = tabs / 2
        val half = (inner - addSlot) / 2
        return List(tabs) { index ->
            val left = index < leftCount
            val count = if (left) leftCount else tabs - leftCount
            if (count == 0) return@List 0.dp
            val equal = (if (count == tabs) inner - addSlot else half) / count
            val sameHalf = selected >= 0 && (selected < leftCount) == left
            when {
                !sameHalf || count == 1 -> equal
                index == selected -> maxOf(equal, (if (count == tabs) inner - addSlot else half) - tabMin * (count - 1))
                else -> minOf(equal, tabMin)
            }
        }
    }

    /** Whether [label] fits the selected tab's pill: its measured width plus a margin on each side. */
    fun fits(labelWidth: Dp, pill: Dp, margin: Dp): Boolean = labelWidth + margin * 2 <= pill
}
