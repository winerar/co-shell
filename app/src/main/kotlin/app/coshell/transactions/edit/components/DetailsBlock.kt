package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellDetailChip
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.ui.format.formatDayMonth

/**
 * «Детали» of the entry sheet in one line (#818, poll «А», board `EntryReceipt`): no card, chips [CoShellDetailChip] —
 * the date and the payee in words; the note and the tags as an icon alone while empty, in words once filled. A chip
 * opens its own small sheet; «Это возврат» moved to the «Сумма» caption, «В шаблон» — to the top row of a saved one.
 */
@Composable
internal fun DetailsBlock(state: TransactionEditUiState, actions: TransactionEditActions) {
    var open by rememberSaveable { mutableStateOf(DetailSheet.NONE) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs)) {
        CoShellDetailChip(
            painterResource(DesignR.drawable.ic_calendar_today), dateText(state), stringResource(R.string.transaction_date),
            onClick = { open = DetailSheet.DATE },
        )
        if (state.payee.isNotBlank()) {
            CoShellDetailChip(
                painterResource(DesignR.drawable.ic_shopping_bag), state.payee, stringResource(R.string.transaction_payee),
                onClick = { open = DetailSheet.PAYEE }, modifier = Modifier.weight(1f, fill = false),
            )
        }
        val note = state.note.trim().lineSequence().firstOrNull()?.takeIf { it.isNotBlank() }
        CoShellDetailChip(
            painterResource(DesignR.drawable.ic_description), note, stringResource(R.string.transaction_note),
            onClick = { open = DetailSheet.NOTE }, modifier = if (note != null) Modifier.weight(1f, fill = false) else Modifier,
        )
        if (!state.isTransfer) {
            val byId = state.tags.options.associateBy { it.id }
            val tags = state.tagIds.mapNotNull { byId[it]?.name }.joinToString().ifEmpty { null }
            CoShellDetailChip(
                painterResource(DesignR.drawable.ic_sell), tags, stringResource(R.string.entry_tags),
                onClick = { open = DetailSheet.TAGS }, modifier = if (tags != null) Modifier.weight(1f, fill = false) else Modifier,
            )
        }
    }
    when (open) {
        DetailSheet.DATE -> DateSheet(state, actions, onClosed = { open = DetailSheet.NONE })
        DetailSheet.NOTE -> NoteSheet(state.note, actions.onNoteChange, onClosed = { open = DetailSheet.NONE })
        DetailSheet.TAGS -> TagPickerSheet(state, actions, onClosed = { open = DetailSheet.NONE })
        DetailSheet.PAYEE -> PayeeSheet(state.payee, onClosed = { open = DetailSheet.NONE })
        DetailSheet.NONE -> Unit
    }
}

private enum class DetailSheet { NONE, DATE, NOTE, TAGS, PAYEE }

/** «Сегодня» / «Вчера» / «3 нояб.»: short, the chip shares its line with three others. */
@Composable
private fun dateText(state: TransactionEditUiState): String = when {
    state.isToday -> stringResource(R.string.transaction_date_today)
    state.isYesterday -> stringResource(R.string.transaction_date_yesterday)
    else -> formatDayMonth(state.date)
}
