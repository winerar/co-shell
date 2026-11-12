package app.coshell.theme.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.ThemeMode

/** «Внешний вид → Тема» (board Fb154Look): system / light / dark as segments across the screen, under the group label. */
@Composable
fun ThemeModePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    val modes = ThemeMode.entries
    CoShellSegmented(
        labels = modes.map { stringResource(it.labelRes) },
        selected = modes.indexOf(selected),
        onSelect = { onSelect(modes[it]) },
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.l),
    )
}

@get:StringRes
internal val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }
