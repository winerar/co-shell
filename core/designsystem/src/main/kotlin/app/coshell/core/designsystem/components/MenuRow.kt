package app.coshell.core.designsystem.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Radii
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

/** The section's icon in a [Sizes.badgeSmall] rounded square washed with its colour. */
@Composable
fun MenuIcon(@DrawableRes icon: Int, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier.size(Sizes.badgeSmall).background(color.copy(alpha = ICON_WASH), RoundedCornerShape(Radii.small)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = color, modifier = Modifier.size(Sizes.tileIcon))
    }
}

private const val ICON_WASH = 0.16f

@Preview(showBackground = true)
@Composable
private fun MenuRowPreview() {
    CoShellTheme {
        CoShellRowGroup {
            row { CoShellMenuRow(R.drawable.ic_category, categoryColor(CategoryPalette.colors[9]), "Категории", {}, subtitle = "Папки, иконки и цвета") }
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
