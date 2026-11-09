package app.coshell.ui.list

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import app.coshell.core.designsystem.components.CoShellSwitch
import app.coshell.core.designsystem.motion.tactileToggleable

/**
 * A settings line with an on/off switch: optional icon, title, optional [subtitle]. The whole row toggles.
 * Sibling of [ActionRow]; reuse it for any boolean setting.
 */
@Composable
fun SwitchRow(
    @DrawableRes icon: Int?,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = icon?.let { { Icon(painterResource(it), contentDescription = null) } },
        // The row handles the click, so the switch itself is display-only (one target for TalkBack).
        trailingContent = { CoShellSwitch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.tactileToggleable(value = checked, onValueChange = onCheckedChange),
    )
}
