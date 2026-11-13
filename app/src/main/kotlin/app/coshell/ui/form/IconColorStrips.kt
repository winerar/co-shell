package app.coshell.ui.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.dashedBorder
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * One line of icons (#290, board `Fb290FA`): six icons in the chosen [color] — the chosen one always among them — and
 * a dashed «Все 52» that opens the whole set ([IconColorSheet]). The form never grows with the icon set.
 */
@Composable
fun IconStrip(selected: String?, color: Long?, onSelect: (String) -> Unit, onAll: () -> Unit, modifier: Modifier = Modifier) {
    val keys = CategoryIcons.keys
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(STRIP_GAP)) {
        stripIcons(keys, selected).forEach { key ->
            IconCell(key, color, key == selected, Modifier.weight(1f).height(ICON_CELL)) { onSelect(key) }
        }
        val shape = MaterialTheme.shapes.medium
        Box(
            Modifier
                .weight(1f)
                .height(ICON_CELL)
                .dashedBorder(MaterialTheme.colorScheme.outline, ICON_CELL / 3)
                .tactileClickable(Tactile.Control, shape, role = Role.Button, onClick = onAll),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.form_all_count, keys.size),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = accentInk(),
                maxLines = 1,
            )
        }
    }
}

/** A filled square with an icon; the chosen one is filled with the colour and draws the icon dark. */
@Composable
internal fun IconCell(key: String, color: Long?, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    val fill = if (selected) color?.let { categoryColor(it) } ?: accentInk() else MaterialTheme.colorScheme.surfaceContainerHigh
    Box(
        modifier
            .tactileClickable(Tactile.Control, shape, role = Role.RadioButton) { onClick() }
            .selectionPop(selected)
            .clip(shape)
            .background(fill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(CategoryIcons.drawableFor(key)),
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(Sizes.tileIcon),
        )
    }
}

/**
 * One line of colours (#290): eight swatches — the chosen one always among them — and «+12» for the rest
 * ([IconColorSheet]). [allowNone] puts «Без цвета» first (accounts).
 */
@Composable
fun ColorStrip(selected: Long?, onSelect: (Long?) -> Unit, onAll: () -> Unit, modifier: Modifier = Modifier, allowNone: Boolean = false) {
    val swatch = selected?.let(CategoryPalette::normalize)
    val shown = stripColors(CategoryPalette.colors, swatch, if (allowNone) STRIP_COLORS - 1 else STRIP_COLORS)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        if (allowNone) NoColorDot(selected == null) { onSelect(null) }
        shown.forEach { color -> ColorDot(color, color == swatch) { onSelect(color) } }
        Text(
            "+${CategoryPalette.colors.size - shown.size}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = accentInk(),
            modifier = Modifier.tactileClickable(Tactile.Control, MaterialTheme.shapes.small, role = Role.Button, onClick = onAll),
        )
    }
}

/** A colour circle; the chosen one shrinks a little inside a ring, so the ring has a gap. */
@Composable
internal fun ColorDot(color: Long, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(DOT_OUTER)
            .tactileClickable(Tactile.Control, CircleShape, role = Role.RadioButton) { onClick() }
            .selectionPop(selected)
            .then(if (selected) Modifier.border(Sizes.selectionRing, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(if (selected) Sizes.swatch - Spacing.xs else Sizes.swatch).clip(CircleShape).background(categoryColor(color)))
    }
}

/** «Без цвета»: an outlined circle with ✕ (✓ when chosen). */
@Composable
internal fun NoColorDot(selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(DOT_OUTER)
            .tactileClickable(Tactile.Control, CircleShape, role = Role.RadioButton) { onClick() }
            .selectionPop(selected),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(Sizes.swatch).border(Sizes.selectionRing, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (selected) DesignR.drawable.ic_check else DesignR.drawable.ic_close),
                contentDescription = stringResource(R.string.color_none),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(Sizes.swatch - Spacing.s),
            )
        }
    }
}

/** The first [STRIP_ICONS] keys; a chosen key further down takes the last place, so it is always seen. */
internal fun stripIcons(keys: List<String>, selected: String?): List<String> {
    val head = keys.take(STRIP_ICONS)
    return if (selected == null || selected in head || selected !in keys) head else head.dropLast(1) + selected
}

/** The first [count] colours; a chosen colour further down takes the last place. */
internal fun stripColors(colors: List<Long>, selected: Long?, count: Int): List<Long> {
    val head = colors.take(count)
    return if (selected == null || selected in head || selected !in colors) head else head.dropLast(1) + selected
}

private const val STRIP_ICONS = 6
private const val STRIP_COLORS = 8
private val ICON_CELL = Sizes.badge
private val STRIP_GAP = Spacing.s - Spacing.xxs
private val DOT_OUTER = Sizes.swatch + Spacing.xs
