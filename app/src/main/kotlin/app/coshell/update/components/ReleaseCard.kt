package app.coshell.update.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.update.AppRelease

/** "What's new" of the release, under the ring and the button. */
@Composable
internal fun ReleaseCard(release: AppRelease, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(stringResource(R.string.update_whats_new_title, release.versionName), style = MaterialTheme.typography.titleMedium)
            Text(release.notes, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
