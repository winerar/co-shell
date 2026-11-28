package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/** One section in a [CoShellMenuGrid]: [mark] sits on the icon's corner (a count or the «новое» pill). */
data class MenuTile(
    @DrawableRes val icon: Int,
    val label: String,
    val onClick: () -> Unit,
    val mark: String? = null,
)

/**
 * Sections as a grid of icons in one card (owner's poll 2026-11-28 «А», boards More, MoreTiles): the icon on the second colour's
 * tone ([MenuIcon], [Sizes.badge]) and a short name under it, up to two lines. Rows of [columns]; a short last row keeps its cells
 * the same width. Fits a whole menu on one screen; the long names and the rarely used — rows of [CoShellMenuRow] next to it.
 */
@Composable
fun CoShellMenuGrid(tiles: List<MenuTile>, modifier: Modifier = Modifier, columns: Int = 4) {
    if (tiles.isEmpty()) return
    Column(
        modifier.fillMaxWidth().rowGroupSegment(RowGroupPosition(0, 1)).padding(vertical = Spacing.s, horizontal = Spacing.xs),
    ) {
        tiles.chunked(columns).forEach { line ->
            Row(Modifier.fillMaxWidth()) {
                line.forEach { tile -> MenuGridCell(tile, Modifier.weight(1f)) }
                repeat(columns - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun MenuGridCell(tile: MenuTile, modifier: Modifier) {
    Column(
        modifier
            .heightIn(min = Sizes.button)
            .tactileClickable(Tactile.Control, shape = MaterialTheme.shapes.large, role = Role.Button, onClick = tile.onClick)
            .padding(vertical = Spacing.s, horizontal = Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        // The box keeps the icon's size: a wide mark («новое») hangs over its corner and does not push the icon off centre.
        Box(Modifier.size(Sizes.badge)) {
            MenuIcon(tile.icon, size = Sizes.badge)
            tile.mark?.let { mark ->
                CoShellStatusChip(
                    mark, StatusTone.Fresh,
                    // Zero-size anchor: the mark ends a little past the icon's right edge and rises above it, so a wide one («новое») covers the
                    // icon's top instead of leaving the cell (the card clips what sticks out).
                    Modifier.layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity))
                        layout(0, 0) { placeable.place((Sizes.badge + Spacing.m).roundToPx() - placeable.width, -Spacing.s.roundToPx()) }
                    },
                )
            }
        }
        Text(
            tile.label,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuGridPreview() {
    CoShellTheme {
        CoShellMenuGrid(
            listOf(
                MenuTile(R.drawable.ic_category, "Счета", {}),
                MenuTile(R.drawable.ic_category, "Категории", {}),
                MenuTile(R.drawable.ic_category, "Бюджеты", {}),
                MenuTile(R.drawable.ic_category, "Цели", {}, mark = "новое"),
                MenuTile(R.drawable.ic_category, "Черновики", {}, mark = "3"),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuGridDarkPreview() {
    CoShellTheme(darkTheme = true) {
        CoShellMenuGrid(listOf(MenuTile(R.drawable.ic_group, "Семья", {}), MenuTile(R.drawable.ic_category, "Регулярные", {})))
    }
}
