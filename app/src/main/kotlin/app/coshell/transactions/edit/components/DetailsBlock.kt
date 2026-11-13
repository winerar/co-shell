package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import app.coshell.core.designsystem.components.CoShellCheckTile
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.ui.format.formatDayMonth

/**
 * «Детали» (#290, board `Fb290A`) — tiles two by two: «Дата», «Заметка», «Теги» and, on an expense, the «Это возврат»
 * check box (owner 2026-11-13). A tile opens its own small sheet; the sheet itself never scrolls. A saved operation
 * gets «Сохранить как шаблон» on the right of the caption.
 */
@Composable
internal fun DetailsBlock(state: TransactionEditUiState, actions: TransactionEditActions, onSaveAsTemplate: () -> Unit) {
    var open by rememberSaveable { mutableStateOf(DetailSheet.NONE) }
    val saveAs: (@Composable () -> Unit)? = if (!state.isNew && state.savedTemplateName == null) {
        { CoShellSectionLink(stringResource(R.string.template_save_as_chip), onClick = onSaveAsTemplate) }
    } else {
        null
    }
    CoShellFormSection(title = stringResource(R.string.entry_section_details), trailing = saveAs) {
        val tiles = buildList<@Composable (Modifier) -> Unit> {
            add { m ->
                CoShellDetailTile(painterResource(DesignR.drawable.ic_calendar_today), stringResource(R.string.transaction_date), dateText(state), { open = DetailSheet.DATE }, m)
            }
            add { m ->
                CoShellDetailTile(
                    painterResource(DesignR.drawable.ic_description), stringResource(R.string.transaction_note),
                    state.note.ifBlank { stringResource(R.string.entry_note_empty) }, { open = DetailSheet.NOTE }, m, empty = state.note.isBlank(),
                )
            }
            if (!state.isTransfer) add { m -> TagsTile(state, m) { open = DetailSheet.TAGS } }
            if (state.type == TransactionType.EXPENSE) {
                add { m ->
                    CoShellCheckTile(
                        state.refund, stringResource(R.string.entry_refund_title), stringResource(R.string.entry_refund_subtitle),
                        actions.onRefundToggle, m,
                    )
                }
            }
        }
        tiles.chunked(2).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs)) {
                line.forEach { tile -> tile(Modifier.weight(1f)) }
                if (line.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
    when (open) {
        DetailSheet.DATE -> DateSheet(state, actions, onClosed = { open = DetailSheet.NONE })
        DetailSheet.NOTE -> NoteSheet(state.note, actions.onNoteChange, onClosed = { open = DetailSheet.NONE })
        DetailSheet.TAGS -> TagPickerSheet(state, actions, onClosed = { open = DetailSheet.NONE })
        DetailSheet.NONE -> Unit
    }
}

private enum class DetailSheet { NONE, DATE, NOTE, TAGS }

/** «Теги · 2» over «Аня, на двоих»; none — «Без тегов». */
@Composable
private fun TagsTile(state: TransactionEditUiState, modifier: Modifier, onClick: () -> Unit) {
    val byId = state.tags.options.associateBy { it.id }
    val chosen = state.tagIds.mapNotNull { byId[it]?.name }
    CoShellDetailTile(
        painterResource(DesignR.drawable.ic_sell),
        if (chosen.isEmpty()) stringResource(R.string.entry_tags) else stringResource(R.string.entry_tags_count, chosen.size),
        chosen.joinToString().ifEmpty { stringResource(R.string.entry_tags_empty) },
        onClick,
        modifier,
        empty = chosen.isEmpty(),
    )
}

/** «Сегодня, 13 ноября» / «Вчера, 12 ноября» / «3 ноября». */
@Composable
private fun dateText(state: TransactionEditUiState): String {
    val day = formatDayMonth(state.date)
    return when {
        state.isToday -> stringResource(R.string.entry_date_today, day)
        state.isYesterday -> stringResource(R.string.entry_date_yesterday, day)
        else -> day
    }
}
