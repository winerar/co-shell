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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** «Отправленные»: the latest messages from this phone, so it's clear what was already reported. */
@Composable
internal fun SentList(entries: List<SentFeedback>) {
    if (entries.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            stringResource(R.string.feedback_sent_title).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        entries.forEach { entry ->
            CoShellListRow(
                title = { Text(entry.text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                supporting = { Text(Instant.ofEpochMilli(entry.sentAtMillis).atZone(ZoneId.systemDefault()).format(format)) },
            )
        }
    }
}
