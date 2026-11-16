package app.coshell.wipe.components

import app.coshell.core.designsystem.category.IconBadge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/** Step 2 of board 18: done, where the copy is, and the way to Wallet import. */
@Composable
internal fun WipeDone(backupName: String?, onImport: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(top = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        IconBadge(DesignR.drawable.ic_check, MaterialTheme.colorScheme.primary, size = Sizes.heroRing / 2)
        Text(stringResource(R.string.wipe_done_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.wipe_done_text, backupName.orEmpty()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CoShellButton(onClick = onImport, modifier = Modifier.fillMaxWidth().padding(top = Spacing.l)) {
            Text(stringResource(R.string.wipe_done_import))
        }
        CoShellOutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.wipe_done_ok))
        }
    }
}
