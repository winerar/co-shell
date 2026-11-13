package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One entity of a form = one card with its own caption (#290, boards `Fb290A`, `Fb290FA`): «СУММА · СПИСАНО»,
 * «КАТЕГОРИЯ · ЧАСТЫЕ», «ДЕТАЛИ». Small grey capitals on the left, an optional [trailing] on the right
 * (a link «Все», the chosen «Кафе ✓»). The card is a [CoShellCard]: it follows its content's height smoothly.
 */
@Composable
fun CoShellFormSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s),
            verticalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs),
        ) {
            Row(Modifier.fillMaxWidth().heightIn(min = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                trailing?.invoke()
            }
            content()
        }
    }
}

/**
 * A small accent link for the right of a [CoShellFormSection] caption («+ Чек», «В шаблон»): a text, not a 48 dp
 * button, so the caption stays one thin line; the tap area is padded around the words.
 */
@Composable
fun CoShellSectionLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = accentInk(),
        maxLines = 1,
        modifier = modifier
            .tactileClickable(Tactile.Control, MaterialTheme.shapes.small, role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
    )
}

/**
 * A detail of a form as a small filled tile (#290, «Детали» 2×2): an icon, a grey [label] over the [value].
 * [empty] greys the value out — «Без заметки». The whole tile is the tap target.
 */
@Composable
fun CoShellDetailTile(
    icon: Painter,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    empty: Boolean = false,
) {
    TileRow(onClick, modifier, Role.Button) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        TileTexts(label, value, valueMuted = empty)
    }
}

/**
 * A yes/no detail as a tile with a check box and words (#290, owner 2026-11-13: «Это возврат» — a plain
 * check box, not a «Возврат: нет» field): [title] with a quiet [subtitle] under it.
 */
@Composable
fun CoShellCheckTile(
    checked: Boolean,
    title: String,
    subtitle: String,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TileRow({ onCheckedChange(!checked) }, modifier, Role.Checkbox) {
        CoShellCheckMark(if (checked) ToggleableState.On else ToggleableState.Off)
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TileRow(onClick: () -> Unit, modifier: Modifier, role: Role, content: @Composable () -> Unit) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier
            .heightIn(min = TILE_HEIGHT)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
            .tactileClickable(Tactile.Control, shape, role = role, onClick = onClick)
            .padding(horizontal = Spacing.s + Spacing.xxs, vertical = Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) { content() }
}

@Composable
private fun TileTexts(label: String, value: String, valueMuted: Boolean) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (valueMuted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val TILE_HEIGHT = 40.dp
