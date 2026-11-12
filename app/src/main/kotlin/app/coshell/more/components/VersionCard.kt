package app.coshell.more.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.tokens.Radii
import androidx.compose.foundation.shape.RoundedCornerShape
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.MenuIcon
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * Top of «Ещё» (board 154А, #155): the installed version, always in sight. A newer build tints the card with the
 * palette's accent and puts «Обновить» on it (starts the download right away, like the pill); a tap elsewhere
 * opens «Обновление приложения» (what's new, manual install, the log).
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
    val accent = MaterialTheme.colorScheme.primary
    val available = availableVersion != null
    CoShellCard(modifier = modifier, tint = if (available) accent else null, onClick = onOpen) {
        Row(
            Modifier.fillMaxWidth().padding(Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (available) {
                Box(
                    Modifier.size(Sizes.badge).background(accent, RoundedCornerShape(Radii.medium)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(DesignR.drawable.ic_download),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(Sizes.buttonIcon),
                    )
                }
            } else {
                MenuIcon(DesignR.drawable.ic_deployed_code_update, categoryColor(CategoryPalette.colors[VERSION_COLOR]))
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
private const val VERSION_COLOR = 9
