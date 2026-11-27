package app.coshell.ui.top

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * «Назад» for a tab screen that is not on the bottom bar right now (#154: «Операции» or «Отчёты» swapped for «Счета»):
 * [TabTopBar] shows the chevron before its title. `null` — the screen is a tab, no chevron. Set by the navigation graph.
 */
val LocalTabBack = staticCompositionLocalOf<(() -> Unit)?> { null }

/** The chevron «‹» of the top bars ([ScreenTopBar], [TabTopBar] off the bar). */
@Composable
internal fun BackButton(onBack: () -> Unit, enabled: Boolean = true) {
    CoShellIconButton(onClick = onBack, enabled = enabled, modifier = Modifier.padding(start = Spacing.s)) {
        Icon(painterResource(DesignR.drawable.ic_chevron_left), stringResource(R.string.action_back))
    }
}
