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
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.update.AppRelease

/** New version number and its "what's new" text. */
@Composable
internal fun ReleaseCard(release: AppRelease, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(
                stringResource(R.string.update_available, release.versionName, release.versionCode),
                style = MaterialTheme.typography.titleMedium,
            )
            if (release.notes.isNotBlank()) Text(release.notes, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Error message in the error container colour. */
@Composable
internal fun ErrorCard(text: String, modifier: Modifier = Modifier) = CoShellMessage(text, isError = true, modifier)
