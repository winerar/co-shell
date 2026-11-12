package app.coshell.feedback.detail

import app.coshell.core.designsystem.components.CoShellSectionLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.feedback.feedbackSteps
import app.coshell.feedback.components.FeedbackStatusChip
import app.coshell.feedback.components.feedbackDate
import app.coshell.feedback.data.SentFeedback
import app.coshell.feedback.splitFeedbackMessage
import app.coshell.ui.top.ScreenTopBar
import kotlin.time.Instant
import app.coshell.core.designsystem.R as DesignR

/**
 * «Отзыв» (board Fb157A2): the message with its kind, date and status, then «Что происходит» —
 * sent → became a task → in work → done in build N; «Убрать из списка» at the bottom.
 */
@Composable
fun FeedbackDetailScreen(entry: SentFeedback?, currentBuild: Int, onBack: () -> Unit, onRemove: () -> Unit) {
    Scaffold(topBar = { ScreenTopBar(stringResource(R.string.feedback_detail_title), onBack = onBack) }) { padding ->
        if (entry == null) return@Scaffold
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            Message(entry)
            CoShellSectionLabel(stringResource(R.string.feedback_detail_progress), Modifier.padding(top = Spacing.s))
            if (entry.documentId.isEmpty()) {
                Text(
                    stringResource(R.string.feedback_detail_no_status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                CoShellCard {
                    FeedbackTimeline(
                        feedbackSteps(Instant.fromEpochMilliseconds(entry.sentAtMillis), entry.status),
                        issueNumber = entry.status?.issueNumber,
                        currentBuild = currentBuild,
                        modifier = Modifier.padding(Spacing.m),
                    )
                }
            }
            CoShellOutlinedButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = null, modifier = Modifier.size(Sizes.buttonIcon))
                Text(stringResource(R.string.feedback_detail_remove), modifier = Modifier.padding(start = Spacing.s))
            }
        }
    }
}

@Composable
private fun Message(entry: SentFeedback) {
    val (kind, body) = splitFeedbackMessage(entry.text)
    CoShellCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.m), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                entry.status?.let { FeedbackStatusChip(it) }
                val date = feedbackDate(entry.sentAtMillis)
                Text(
                    if (kind != null) "$kind · $date" else date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(body, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
