package app.coshell.release

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * «Версия N · Что нового · 5 историй» at the top of «Обзор» (#693, owner poll 2026-11-24 «Б», board `Rel693NoticeB`): once after an update,
 * «Смотреть» opens the stories, «✕» — not now, for good. Shown by [ReleaseNoticeViewModel].
 */
@Composable
fun ReleaseNoticeCard(versionName: String, count: Int, onOpen: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(start = Spacing.l, top = Spacing.m, bottom = Spacing.m, end = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.release_notice_version, versionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(pluralStringResource(R.plurals.release_notice_title, count, count), style = MaterialTheme.typography.titleMedium)
            }
            CoShellButton(onClick = onOpen) { Text(stringResource(R.string.release_notice_open), maxLines = 1) }
            CoShellIconButton(onClick = onDismiss) {
                Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.release_notice_dismiss))
            }
        }
    }
}

/** What the card says: this build's version and how many stories wait. */
data class ReleaseNotice(val versionName: String, val count: Int)
