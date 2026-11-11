package app.coshell.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.feedback.data.SentFeedback
import app.coshell.feedback.splitFeedbackMessage

/** «Мои отзывы» (board Fb157A): text, kind and date, status pill; tap — the card, long press — remove. */
@Composable
internal fun SentList(entries: List<SentFeedback>, onOpen: (Long) -> Unit, onRemove: (Long) -> Unit) {
    if (entries.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            stringResource(R.string.feedback_sent_title, entries.size).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        entries.forEach { entry ->
            val (kind, body) = splitFeedbackMessage(entry.text)
            val date = feedbackDay(entry.sentAtMillis)
            CoShellListRow(
                title = { Text(body, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supporting = { Text(if (kind != null) "$kind · $date" else date) },
                trailing = entry.status?.let { { FeedbackStatusChip(it) } },
                onClick = { onOpen(entry.sentAtMillis) },
                onLongClick = { onRemove(entry.sentAtMillis) },
            )
        }
        Text(
            stringResource(R.string.feedback_sent_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
