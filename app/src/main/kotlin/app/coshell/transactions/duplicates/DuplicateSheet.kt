package app.coshell.transactions.duplicates

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.transactions.list.TransactionsUiState.RowDuplicate
import app.coshell.ui.sheet.ModalSheet

/**
 * «Похоже на дубль» (#791, poll 2026-11-27 «А», board `Smart791Mark`): two alike records — remove the newer one (with «Отменить»),
 * say they are different (not asked again), or open the tapped one as usual.
 */
@Composable
internal fun DuplicateSheet(
    duplicate: RowDuplicate,
    tapped: String,
    onDelete: (String) -> Unit,
    onDifferent: (String) -> Unit,
    onOpen: (String) -> Unit,
    onClosed: () -> Unit,
) {
    ModalSheet(title = stringResource(R.string.duplicate_title), onClosed = onClosed) { close ->
        Text(stringResource(R.string.duplicate_text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        CoShellButton(onClick = { onDelete(duplicate.newer); close() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.duplicate_delete))
        }
        CoShellOutlinedButton(onClick = { onDifferent(duplicate.key); close() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.duplicate_different))
        }
        CoShellTextButton(onClick = { onOpen(tapped); close() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.duplicate_open))
        }
    }
}
