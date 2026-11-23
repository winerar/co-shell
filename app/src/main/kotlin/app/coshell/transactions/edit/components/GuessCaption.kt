package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.ui.ai.BetaMark
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.categoryguess.CategoryGuess

/**
 * The quiet grey line in the «Категория» card (#584, board `AiCapA`, poll 2026-11-20 «А» + 2026-11-21 «А»): where an automatic
 * category came from — «по прошлым операциям» (history) or «по названию» (brand dictionary). Gone after a manual pick.
 */
@Composable
internal fun GuessCaption(source: CategoryGuess.Source) {
    val text = stringResource(
        if (source == CategoryGuess.Source.HISTORY) R.string.entry_category_guess_history else R.string.entry_category_guess_name,
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(painterResource(DesignR.drawable.ic_history), contentDescription = null, Modifier.size(Spacing.l), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** The same line when the model chose (#594, board `EntryGuessAi`): a spark, «предложено ИИ» and the «бета» mark (#643). */
@Composable
internal fun AiGuessCaption() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Icon(painterResource(DesignR.drawable.ic_spark), contentDescription = null, Modifier.size(Spacing.l), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            stringResource(R.string.entry_category_guess_ai), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        BetaMark()
    }
}
