package app.coshell.ui.top

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.R as DesignR

/**
 * Header of an inner screen (Курсы, Настройки, Семья…): «Назад» and a 22 sp bold title, as on the mockup
 * boards (review 2026-11-10, К1). Tabs use [TabTopBar].
 *
 * [onBack] null hides «Назад» (a finished wipe has nowhere to go back to); [backEnabled] false greys it out
 * while work must not be interrupted. [subtitle] — a second muted line (the import wizard's step).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    title: String,
    onBack: (() -> Unit)?,
    backEnabled: Boolean = true,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
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
                CoShellIconButton(onClick = onBack, enabled = backEnabled) {
                    Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                }
            }
        },
        actions = actions,
    )
}
