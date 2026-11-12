package app.coshell.theme.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.designsystem.R as DesignR

/** Settings line «Тема» with a system / light / dark switch under the title. */
@Composable
fun ThemeModeRow(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val modes = ThemeMode.entries
    CoShellListRow(
        leading = { Icon(painterResource(DesignR.drawable.ic_contrast), contentDescription = null) },
        title = { Text(stringResource(R.string.theme_title)) },
        supporting = {
            CoShellSegmented(
                labels = modes.map { stringResource(it.labelRes) },
                selected = modes.indexOf(selected),
                onSelect = { onSelect(modes[it]) },
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.s),
            )
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
