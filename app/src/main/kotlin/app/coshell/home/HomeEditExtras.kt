package app.coshell.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.settings.HomeCard
import app.coshell.ui.sheet.ModalSheet

// Extras of the editing mode on «Обзор» (poll #154 2026-11-27, all «А»; boards Home154BackA, Home154Empty).

/** Over «Готово»: «Скрыто: N · Вернуть». Opens the sheet with the hidden cards; nothing is shown when none is hidden. */
@Composable
internal fun HiddenCardsBar(hidden: List<HomeCard>, onShow: (HomeCard) -> Unit, modifier: Modifier = Modifier) {
    if (hidden.isEmpty()) return
    var open by rememberSaveable { mutableStateOf(false) }
    CoShellCard(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(start = Spacing.l, end = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.home_edit_hidden, hidden.size), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            CoShellTextButton(onClick = { open = true }) { Text(stringResource(R.string.home_edit_restore)) }
        }
    }
    if (open) HiddenCardsSheet(hidden, onShow, onClosed = { open = false })
}

/** The hidden cards, each with «Показать»; the sheet closes with the last one. */
@Composable
private fun HiddenCardsSheet(hidden: List<HomeCard>, onShow: (HomeCard) -> Unit, onClosed: () -> Unit) {
    ModalSheet(title = stringResource(R.string.home_edit_hidden_title), onClosed = onClosed) { close ->
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            hidden.forEach { card ->
                CoShellListRow(
                    title = stringResource(card.titleRes()),
                    trailing = {
                        CoShellTextButton(onClick = {
                            onShow(card)
                            if (hidden.size == 1) close()
                        }) { Text(stringResource(R.string.home_edit_show)) }
                    },
                )
            }
        }
    }
}

/** What a card with nothing to say now shows in the editing mode (dimmed by the caller): it can still be moved or hidden. */
@Composable
internal fun EmptyCardNote() {
    Text(
        stringResource(R.string.home_edit_empty),
        Modifier.fillMaxWidth().padding(Spacing.l),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
