package app.coshell.ui.list

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.R as DesignR

/**
 * A tappable menu line: icon, title, optional [subtitle], and a chevron when it opens another screen.
 * Used by "More" and the backup screen; reuse it for any settings-like list.
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
    val alpha = if (enabled) 1f else 0.38f
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(painterResource(icon), contentDescription = null) },
        trailingContent = if (opensScreen) {
            { Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null) }
        } else {
            null
        },
        colors = ListItemDefaults.colors(
            headlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
        ),
        modifier = Modifier.tactileClickable(enabled = enabled, onClick = onClick),
    )
}
