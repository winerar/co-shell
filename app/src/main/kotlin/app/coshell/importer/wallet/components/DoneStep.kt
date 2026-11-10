package app.coshell.importer.wallet.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.ui.hero.StatusCard
import app.coshell.core.designsystem.R as DesignR

/** Step 7: how many were added, with «Отменить импорт» right there. */
fun LazyListScope.doneStep(added: Int, onUndo: () -> Unit) {
    item(key = "done") {
        StatusCard(
            icon = DesignR.drawable.ic_check,
            title = stringResource(R.string.import_done_title),
            subtitle = stringResource(R.string.import_done_text, added),
            tint = MaterialTheme.colorScheme.tertiary,
            below = { CoShellTextButton(onClick = onUndo) { Text(stringResource(R.string.import_undo)) } },
        )
    }
}
