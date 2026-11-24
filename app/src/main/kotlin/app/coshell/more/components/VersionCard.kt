package app.coshell.more.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * Top of «Ещё» while a newer build is out (poll 2026-11-20, Б; board MoreBUpdate): the card shows the download icon
 * and «Обновить» (starts the download right away, like the pill); a tap elsewhere
 * opens «Обновление» (what's new, manual install, the log).
 */
@Composable
internal fun VersionCard(
    versionName: String,
    build: Int,
    availableVersion: String?,
    onOpen: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val available = availableVersion != null
    CoShellCard(modifier = modifier, onClick = onOpen) {
        Row(
            Modifier.fillMaxWidth().padding(Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (available) {
                MenuIcon(DesignR.drawable.ic_download)
            } else {
                MenuIcon(DesignR.drawable.ic_deployed_code_update)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                Text(
                    stringResource(R.string.more_version_title),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (availableVersion != null) {
                        stringResource(R.string.more_version_available, availableVersion)
                    } else {
                        stringResource(R.string.version_label, versionName, build)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (available) {
                CoShellButton(onClick = onUpdate) { Text(stringResource(R.string.update_open)) }
            } else {
                Icon(
                    painterResource(DesignR.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Lime-green of the palette, as «Категории»: the app's own colour. */

/** Bottom of «Ещё» when the app is up to date (board MoreBEnd): one quiet line; a tap opens «Обновление» (log, past versions). */
@Composable
internal fun QuietVersion(versionName: String, build: Int, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        stringResource(R.string.more_version_quiet, versionName, build),
        modifier = modifier
            .tactileClickable(onClick = onOpen)
            .fillMaxWidth()
            .heightIn(min = Sizes.button)
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}
