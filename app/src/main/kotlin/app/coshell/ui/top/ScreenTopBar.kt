package app.coshell.ui.top

import androidx.compose.foundation.layout.Arrangement
import app.coshell.core.designsystem.theme.ScreenTitleStyle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTopBar
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * Header of an inner screen (Курсы, Настройки, Семья…): «Назад» and a 22 sp bold title, as on the mockup
 * boards (review 2026-11-10, К1). Tabs use [TabTopBar].
 *
 * Solid like the tab headers; once the screen's content moves under it, a hairline fades in (#394 Б) — the scroll
 * comes from [LocalScreenScroll], set for every destination by the navigation host.
 *
 * [onBack] null hides «Назад» (a finished wipe has nowhere to go back to); [backEnabled] false greys it out
 * while work must not be interrupted. [subtitle] — a second muted line (the import wizard's step).
 */
@Composable
fun ScreenTopBar(
    title: String,
    onBack: (() -> Unit)?,
    backEnabled: Boolean = true,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    CoShellTopBar(
        title = {
            Column {
                Text(
                    title,
                    style = ScreenTitleStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        navigationIcon = {
            if (onBack != null) {
                CoShellIconButton(onClick = onBack, enabled = backEnabled, modifier = Modifier.padding(start = Spacing.s)) {
                    Icon(painterResource(DesignR.drawable.ic_chevron_left), stringResource(R.string.action_back))
                }
            }
        },
        actions = { TopBarActions(actions) },
    )
}

/** Actions of a top bar: 48 dp circles (board Style6) with a small gap between them and from the edge. */
@Composable
internal fun TopBarActions(actions: @Composable RowScope.() -> Unit) {
    Row(Modifier.padding(end = Spacing.s), horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        actions()
    }
}
