package app.coshell.importer.wallet.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.importer.wallet.UnpairedChoice
import app.coshell.core.domain.importer.wallet.WalletRecord
import app.coshell.core.model.Account
import app.coshell.core.model.MoneyFormat
import app.coshell.importer.wallet.WalletImportDraft
import app.coshell.ui.format.formatShortDate

/**
 * Step 5 (only when the file has them): transfers whose other half is missing — «Корректировка»
 * (default), a transfer with an existing account ([onPickAccount] opens the list), or «Пропустить».
 */
fun LazyListScope.transfersStep(
    draft: WalletImportDraft,
    existing: List<Account>,
    moneyFormat: MoneyFormat,
    onChoose: (Int, UnpairedChoice) -> Unit,
    onPickAccount: (WalletRecord) -> Unit,
) {
    item(key = "hint") { CoShellHint(stringResource(R.string.import_transfers_hint), Modifier.padding(horizontal = Spacing.l)) }
    items(draft.unpaired, key = { "unp-${it.line}" }) { record ->
        UnpairedRow(record, draft.unpairedChoices[record.line] ?: UnpairedChoice.Adjustment, existing, moneyFormat, onChoose, onPickAccount)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UnpairedRow(
    record: WalletRecord,
    choice: UnpairedChoice,
    existing: List<Account>,
    moneyFormat: MoneyFormat,
    onChoose: (Int, UnpairedChoice) -> Unit,
    onPickAccount: (WalletRecord) -> Unit,
) {
    Column(Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text("${record.account} · ${moneyFormat.format(record.amount)}", style = MaterialTheme.typography.bodyLarge)
        Text(
            listOf(formatShortDate(record.dateTime.date), record.note).filter { it.isNotBlank() }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CoShellFilterChip(
                selected = choice == UnpairedChoice.Adjustment,
                onClick = { onChoose(record.line, UnpairedChoice.Adjustment) },
                label = { Text(stringResource(R.string.import_unpaired_adjustment)) },
            )
            val target = (choice as? UnpairedChoice.TransferWith)?.let { c -> existing.firstOrNull { it.id == c.accountId } }
            CoShellFilterChip(
                selected = target != null,
                onClick = { onPickAccount(record) },
                label = {
                    Text(target?.let { stringResource(R.string.import_unpaired_transfer, it.name) } ?: stringResource(R.string.import_unpaired_transfer_choose))
                },
            )
            CoShellFilterChip(
                selected = choice == UnpairedChoice.Skip,
                onClick = { onChoose(record.line, UnpairedChoice.Skip) },
                label = { Text(stringResource(R.string.import_unpaired_skip)) },
            )
        }
    }
}

/** Existing co-shell accounts as the other side of an unpaired transfer. */
@Composable
fun UnpairedAccountDialog(selected: UnpairedChoice?, existing: List<Account>, onPick: (UnpairedChoice) -> Unit, onDismiss: () -> Unit) {
    val options = existing.map { ChoiceOption<UnpairedChoice>(UnpairedChoice.TransferWith(it.id), it.name, it.currency.value) }
    ChoiceDialog(stringResource(R.string.import_unpaired_pick_title), options, selected, onPick, onDismiss)
}
