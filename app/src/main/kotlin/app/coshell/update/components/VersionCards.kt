package app.coshell.update.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.update.AppRelease
import app.coshell.ui.format.formatDayMonth
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** «Сейчас 0.36 · сборка 399 → Новая 0.37 · сборка 400» (board Fb268A). */
@Composable
internal fun VersionCompareCard(installedName: String, installedCode: Int, release: AppRelease, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            VersionColumn(stringResource(R.string.update_version_now), installedName, installedCode, Modifier.weight(1f))
            Text("→", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            VersionColumn(
                stringResource(R.string.update_version_new),
                release.versionName,
                release.versionCode,
                Modifier.weight(1f).padding(start = Spacing.l),
                accentInk(),
            )
        }
    }
}

/**
 * «Ваша версия 0.37 · сборка 400 · установлена 12 ноября» (board Fb268B); under a line — «Прошлые версии ›» (#284),
 * when [onPastReleases] is set.
 */
@Composable
internal fun InstalledVersionCard(
    name: String,
    code: Int,
    installedAt: Instant?,
    modifier: Modifier = Modifier,
    onPastReleases: (() -> Unit)? = null,
) {
    CoShellCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            VersionColumn(stringResource(R.string.update_version_yours), name, code, Modifier.weight(1f))
            installedAt?.let {
                Text(
                    stringResource(R.string.update_version_installed_on, formatDayMonth(it.toLocalDateTime(TimeZone.currentSystemDefault()).date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
        onPastReleases?.let { PastReleasesRow(it) }
    }
}

@Composable
private fun PastReleasesRow(onClick: () -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    HorizontalDivider(Modifier.padding(horizontal = Spacing.l), color = MaterialTheme.colorScheme.outlineVariant)
    Row(
        Modifier.fillMaxWidth().tactileClickable(onClick = onClick).padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.update_past_row), style = MaterialTheme.typography.bodyMedium, color = muted, modifier = Modifier.weight(1f))
        Icon(painterResource(DesignR.drawable.ic_chevron_right), contentDescription = null, tint = muted)
    }
}

@Composable
private fun VersionColumn(label: String, name: String, code: Int, modifier: Modifier, color: Color = Color.Unspecified) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = muted)
        Text(name, style = AmountTitleStyle, color = color)
        Text(stringResource(R.string.update_version_build, code), style = MaterialTheme.typography.bodySmall, color = muted)
    }
}
