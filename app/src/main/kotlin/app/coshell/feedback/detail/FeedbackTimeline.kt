package app.coshell.feedback.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.feedback.FeedbackStep
import app.coshell.feedback.components.feedbackDate

/** Vertical steps with dots and a connecting line: reached — lime, ahead — grey (board Fb157A2). */
@Composable
internal fun FeedbackTimeline(steps: List<FeedbackStep>, issueNumber: Int?, currentBuild: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        steps.forEachIndexed { index, step ->
            val next = steps.getOrNull(index + 1)
            Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                Dot(reached = step.reached, lineReached = next?.reached, modifier = Modifier.fillMaxHeight())
                Column(Modifier.padding(bottom = if (next != null) Spacing.m else Spacing.xxs)) {
                    Text(
                        stepTitle(step, issueNumber),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (step.reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    stepNote(step, currentBuild)?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** The dot and, below it, the line to the next step ([lineReached] `null` — last step, no line). */
@Composable
private fun Dot(reached: Boolean, lineReached: Boolean?, modifier: Modifier) {
    val on = MaterialTheme.colorScheme.primary
    val off = MaterialTheme.colorScheme.outlineVariant
    Box(modifier.width(DOT), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.width(DOT).fillMaxHeight()) {
            val r = size.width / 2
            val cy = r + Spacing.xs.toPx()
            if (lineReached != null) {
                drawLine(if (lineReached) on else off, Offset(r, cy), Offset(r, size.height), strokeWidth = Sizes.selectionRing.toPx())
            }
            drawCircle(if (reached) on else off, radius = r, center = Offset(r, cy))
        }
    }
}

@Composable
private fun stepTitle(step: FeedbackStep, issueNumber: Int?): String = when (step.kind) {
    FeedbackStep.Kind.SENT -> stringResource(R.string.feedback_step_sent)
    FeedbackStep.Kind.TASK -> issueNumber?.let { stringResource(R.string.feedback_step_task_number, it) }
        ?: stringResource(R.string.feedback_step_task)
    FeedbackStep.Kind.IN_PROGRESS -> stringResource(R.string.feedback_step_in_progress)
    FeedbackStep.Kind.DONE -> step.build?.let { stringResource(R.string.feedback_step_done_build, it) }
        ?: stringResource(R.string.feedback_step_done)
    FeedbackStep.Kind.LATER -> stringResource(R.string.feedback_step_later)
    FeedbackStep.Kind.CLOSED -> stringResource(R.string.feedback_step_closed)
}

/** Date of the step; for a reached «done» — whether this phone already has it. */
@Composable
private fun stepNote(step: FeedbackStep, currentBuild: Int): String? {
    val date = step.at?.let(::feedbackDate)
    val build = step.build
    val hint = when {
        step.kind != FeedbackStep.Kind.DONE || !step.reached -> null
        build == null -> stringResource(R.string.feedback_step_done_next)
        build > currentBuild -> stringResource(R.string.feedback_step_done_update)
        else -> null
    }
    return listOfNotNull(date, hint).joinToString(" · ").ifEmpty { null }
}

private val DOT = Sizes.timelineDot
