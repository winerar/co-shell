package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.ButtonTone
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.UnsavedUi
import app.coshell.core.designsystem.R as DesignR

/**
 * «Не сохранено» (#323, board `EwDraftA`): the sheet closed with something typed — the next new one opens empty with this plate on top.
 * «Продолжить» puts the amount, account and the rest back, «✕» forgets the draft. It lives up to 12 hours, one, on this phone only.
 */
@Composable
internal fun UnsavedPlate(unsaved: UnsavedUi, onContinue: () -> Unit, onDiscard: () -> Unit) {
    val discard = stringResource(R.string.unsaved_discard)
    CoShellCard(Modifier.fillMaxWidth().padding(horizontal = Spacing.l)) {
        Row(
            Modifier.padding(start = Spacing.m, top = Spacing.s, bottom = Spacing.s),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.unsaved_title), style = MaterialTheme.typography.labelLarge)
                Text(
                    listOfNotNull(unsaved.amount, unsaved.label, ago(unsaved.minutes)).joinToString(" \u00B7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CoShellButton(onClick = onContinue, tone = ButtonTone.Tonal) { Text(stringResource(R.string.unsaved_continue)) }
            CoShellIconButton(onClick = onDiscard, modifier = Modifier.semantics { contentDescription = discard }) {
                Icon(painterResource(DesignR.drawable.ic_close), contentDescription = null)
            }
        }
    }
}

@Composable
private fun ago(minutes: Int): String = when {
    minutes < 1 -> stringResource(R.string.unsaved_now)
    minutes < 60 -> stringResource(R.string.unsaved_minutes, minutes)
    else -> stringResource(R.string.unsaved_hours, minutes / 60)
}
