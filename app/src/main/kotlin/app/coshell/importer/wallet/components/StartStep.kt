package app.coshell.importer.wallet.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.importer.ImportBatch
import app.coshell.importer.wallet.components.ImportColors.FILE
import app.coshell.more.components.MoreTile
import app.coshell.ui.format.formatShortDate
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import app.coshell.core.designsystem.R as DesignR

/** Step 1: how to get the CSV out of Wallet, «Выбрать файл», and earlier imports with «Отменить». */
fun LazyListScope.startStep(batches: List<ImportBatch>, timeZone: TimeZone, onPickFile: () -> Unit, onUndo: (ImportBatch) -> Unit) {
    item(key = "intro") { CoShellHint(stringResource(R.string.import_start_intro), Modifier.padding(horizontal = Spacing.l)) }
    item(key = "pick") {
        MoreTile(
            DesignR.drawable.ic_file_open,
            stringResource(R.string.import_pick_file),
            stringResource(R.string.import_pick_file_hint),
            CategoryPalette.colors[FILE],
            onClick = onPickFile,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.l, vertical = Spacing.s),
        )
    }
    if (batches.isNotEmpty()) {
        item(key = "batches") { SectionTitle(stringResource(R.string.import_batches_title)) }
        items(batches, key = { "batch-${it.id}" }) { batch ->
            fun day(i: Instant) = formatShortDate(i.toLocalDateTime(timeZone).date)
            CoShellListRow(
                title = stringResource(R.string.import_batch_row, day(batch.importedAt), batch.count),
                subtitle = stringResource(R.string.import_batch_period, day(batch.firstDate), day(batch.lastDate)),
                trailing = { CoShellTextButton(onClick = { onUndo(batch) }) { Text(stringResource(R.string.import_undo)) } },
            )
        }
    }
}
