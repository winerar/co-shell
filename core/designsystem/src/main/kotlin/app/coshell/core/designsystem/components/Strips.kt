package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.ColorFill
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.category.inkOn
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileCircleClickable
import app.coshell.core.designsystem.theme.selectionColors
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

// Choosing a colour or an icon (board `CompNew` «Выбор иконки и цвета», #367 А; poll «Чем заменить самодельное», 2026-11-16).

/**
 * One line of colours: circles of [Sizes.badge] — the chosen one always among them, in a ring with a check mark — and
 * «+N» ([CoShellTagMoreChip]) for the rest. As many circles as fit the width. [allowNone] puts «Без цвета» first (accounts).
 */
@Composable
fun CoShellColorStrip(
    selected: Long?,
    onSelect: (Long?) -> Unit,
    onAll: () -> Unit,
    noneLabel: String,
    modifier: Modifier = Modifier,
    allowNone: Boolean = false,
) {
    val swatch = selected?.let(CategoryPalette::normalize)
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val slots = (stripSlots(maxWidth, Sizes.badge) - if (allowNone) 1 else 0).coerceAtLeast(MIN_SLOTS)
        val shown = stripColors(CategoryPalette.colors, swatch, slots)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
            if (allowNone) CoShellNoColorDot(selected == null, noneLabel) { onSelect(null) }
            shown.forEach { color -> CoShellColorDot(color, color == swatch) { onSelect(color) } }
            CoShellTagMoreChip("+${CategoryPalette.colors.size - shown.size}", onAll)
        }
    }
}

/**
 * One line of icons: circles of [Sizes.cellHeight] in the chosen [color] — the chosen one always among them — and
 * «Все 52» ([CoShellTagMoreChip]). The form never grows with the icon set.
 */
@Composable
fun CoShellIconStrip(
    selected: String?,
    onSelect: (String) -> Unit,
    onAll: () -> Unit,
    allLabel: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val shown = stripIcons(CategoryIcons.keys, selected, stripSlots(maxWidth, Sizes.cellHeight).coerceAtLeast(MIN_SLOTS))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
            shown.forEach { key -> CoShellIconCell(key, key == selected) { onSelect(key) } }
            CoShellTagMoreChip(allLabel, onAll)
        }
    }
}

/** A palette colour as a circle of [size]; the chosen one shrinks inside a ring of the ink and shows a check mark. */
@Composable
fun CoShellColorDot(color: Long, selected: Boolean, size: Dp = Sizes.badge, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .tactileCircleClickable(role = Role.RadioButton, onClick = onClick)
            .selectionPop(selected)
            .then(if (selected) Modifier.border(Sizes.selectionRing, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        ColorFill(color, size = if (selected) size - Spacing.xs * 2 else size - Spacing.xs) {
            if (selected) Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = inkOn(categoryColor(color)), modifier = Modifier.size(size / 2))
        }
    }
}

/** «Без цвета»: an outlined circle with ✕ (✓ when chosen). */
@Composable
fun CoShellNoColorDot(selected: Boolean, label: String, size: Dp = Sizes.badge, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .tactileCircleClickable(role = Role.RadioButton, onClick = onClick)
            .selectionPop(selected),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(size - Spacing.xs).border(Sizes.selectionRing, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (selected) R.drawable.ic_check else R.drawable.ic_close),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(size / 2),
            )
        }
    }
}

/** An icon in a circle of [size] on the track; the chosen one — like every chosen: neon, a ring of the ink. */
@Composable
fun CoShellIconCell(key: String, selected: Boolean, size: Dp = Sizes.cellHeight, onClick: () -> Unit) {
    val sel = selectionColors()
    Box(
        Modifier
            .size(size)
            .tactileCircleClickable(role = Role.RadioButton, onClick = onClick)
            .selectionPop(selected)
            .background(if (selected) sel.fill else MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
            .then(if (selected) Modifier.border(Sizes.selectionRing, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(CategoryIcons.drawableFor(key)),
            contentDescription = null,
            tint = if (selected) sel.content else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Sizes.tileIcon),
        )
    }
}

/** How many circles of [cell] fit [width] next to the «+N» chip ([MORE_WIDTH]). */
internal fun stripSlots(width: Dp, cell: Dp): Int = ((width - MORE_WIDTH + Spacing.s) / (cell + Spacing.s)).toInt()

/** The first [count] icon keys; a chosen key further down takes the last place, so it is always seen. */
internal fun stripIcons(keys: List<String>, selected: String?, count: Int): List<String> {
    val head = keys.take(count)
    return if (selected == null || selected in head || selected !in keys) head else head.dropLast(1) + selected
}

/** The first [count] colours; a chosen colour further down takes the last place. */
internal fun stripColors(colors: List<Long>, selected: Long?, count: Int): List<Long> {
    val head = colors.take(count)
    return if (selected == null || selected in head || selected !in colors) head else head.dropLast(1) + selected
}

private const val MIN_SLOTS = 3
private val MORE_WIDTH = Sizes.cellHeight + Spacing.l

/** A bank or payment-app preset: the account badge with its initials; the chosen one gets a ring of its own colour. */
@Composable
fun CoShellBankDot(name: String, color: Long, selected: Boolean, size: Dp = Sizes.badgeSmall, onClick: () -> Unit) {
    Box(
        Modifier
            .semantics { contentDescription = name }
            .tactileCircleClickable(role = Role.RadioButton, onClick = onClick)
            .selectionPop(selected)
            .border(Sizes.selectionRing, if (selected) categoryColor(color) else Color.Transparent, CircleShape),
    ) {
        AccountBadge(name, color, size = size)
    }
}
