package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes

/**
 * A section in a menu («Ещё», board 154А, #155): the icon in a soft rounded square of its [color], title, one grey line
 * about the section, and a chevron — or [trailing] instead (a «new» dot). Dense (52 dp) to fit a menu on one screen;
 * put several in a [CoShellRowGroup]. The square tells a section apart from a category (those are circles).
 */
@Composable
fun CoShellMenuRow(
    @DrawableRes icon: Int,
    color: Color,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    CoShellListRow(
        title = title,
        subtitle = subtitle,
        subtitleMaxLines = 1,
        modifier = modifier,
        leading = { MenuIcon(icon, color) },
        trailing = trailing ?: { Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null) },
        onClick = onClick,
        dense = true,
    )
}

/** The section's icon in a [Sizes.badgeSmall] circle tinted with its colour — the same holder as [IconBadge] (board Style3). */
@Composable
fun MenuIcon(@DrawableRes icon: Int, color: Color, modifier: Modifier = Modifier) {
    IconBadge(icon, color, modifier, Sizes.badgeSmall)
}

@Preview(showBackground = true)
@Composable
private fun MenuRowPreview() {
    CoShellTheme {
        CoShellRowGroup {
            row { CoShellMenuRow(R.drawable.ic_category, categoryColor(CategoryPalette.colors[9]), "Категории", {}, subtitle = "Подкатегории, иконки и цвета") }
            row { CoShellMenuRow(R.drawable.ic_group, categoryColor(CategoryPalette.colors[3]), "Семья", {}, subtitle = "Общий бюджет на двух телефонах") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MenuRowDarkPreview() {
    CoShellTheme(darkTheme = true) {
        CoShellRowGroup {
            row { CoShellMenuRow(R.drawable.ic_contrast, categoryColor(CategoryPalette.colors[2]), "Внешний вид", {}, subtitle = "Вольт · тёмная") }
        }
    }
}
