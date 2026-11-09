package app.coshell.ui.list

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.R as DesignR

/**
 * A tappable menu line: icon, title, optional [subtitle], and a chevron when it opens another screen.
 * Used by "More", settings, backup, family; reuse it for any settings-like list.
 */
@Composable
fun ActionRow(
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    opensScreen: Boolean = false,
    enabled: Boolean = true,
) {
    CoShellListRow(
        title = title,
        subtitle = subtitle,
        leading = { Icon(painterResource(icon), contentDescription = null) },
        trailing = if (opensScreen) {
            { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) }
        } else {
            null
        },
        onClick = onClick,
        enabled = enabled,
    )
}
