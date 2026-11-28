package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.secondaryInk
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A section in a menu («Ещё», board 154А, #155): the icon on a tone of the theme's second colour (#442 Б), title, one grey line
 * about the section, and a chevron — or [trailing] instead. [mark] goes right after the title (the «новое» pill, #335).
 * Dense (52 dp) to fit a menu on one screen;
 * put several in a [CoShellRowGroup]. The square tells a section apart from a category (those are circles).
 */
@Composable
fun CoShellMenuRow(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    mark: (@Composable () -> Unit)? = null,
) {
    CoShellListRow(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text(title, Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
                mark?.invoke()
            }
        },
        supporting = subtitle?.let { { Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
        modifier = modifier,
        leading = { MenuIcon(icon) },
        trailing = trailing ?: { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null) },
        onClick = onClick,
        dense = true,
    )
}

/**
 * The section's icon in a [size] circle ([Sizes.badgeSmall] in rows, [Sizes.badge] in [CoShellMenuGrid]) (board Style3): the glyph in the theme's second colour on its 18 % tone —
 * menus speak in the second colour, the primary is kept for «now and action» (owner's poll 2026-11-23, #442 Б).
 */
@Composable
fun MenuIcon(@DrawableRes icon: Int, modifier: Modifier = Modifier, size: Dp = Sizes.badgeSmall) {
    val ink = secondaryInk()
    Box(
        modifier.size(size).background(lerp(MaterialTheme.colorScheme.surface, ink, MENU_TONE), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = ink, modifier = Modifier.size(size * MENU_GLYPH))
    }
}

private const val MENU_TONE = 0.18f
private const val MENU_GLYPH = 0.55f

@Preview(showBackground = true)
@Composable
private fun MenuRowPreview() {
    CoShellTheme {
        CoShellRowGroup {
            row { CoShellMenuRow(R.drawable.ic_category, "Категории", {}, subtitle = "Подкатегории, иконки и цвета") }
            row {
                CoShellMenuRow(
                    R.drawable.ic_group, "Семья", {}, subtitle = "Общий бюджет на двух телефонах",
                    mark = { CoShellStatusChip("новое", StatusTone.Fresh) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuRowDarkPreview() {
    CoShellTheme(darkTheme = true) {
        CoShellRowGroup {
            row { CoShellMenuRow(R.drawable.ic_contrast, "Внешний вид", {}, subtitle = "Вольт · тёмная") }
        }
    }
}
