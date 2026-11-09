package app.coshell.theme.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.designsystem.R as DesignR

/** Settings line «Тема» with a system / light / dark switch under the title. */
@Composable
fun ThemeModeRow(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val modes = ThemeMode.entries
    ListItem(
        leadingContent = { Icon(painterResource(DesignR.drawable.ic_contrast), contentDescription = null) },
        headlineContent = { Text(stringResource(R.string.theme_title)) },
        supportingContent = {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = Spacing.s)) {
                modes.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = mode == selected,
                        onClick = { onSelect(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                        icon = {}, // no filled check mark: icons are outlined only (code-style.md)
                        label = { Text(stringResource(mode.labelRes)) },
                    )
                }
            }
        },
    )
}

@get:StringRes
private val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }
