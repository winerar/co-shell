package app.coshell.core.domain.feedback

import kotlin.time.Instant

/**
 * Where a sent message is on its way to the app (#157, board Fb157A). The robot that turns messages
 * into GitHub issues (`scripts/feedback_to_issues.py`) writes it back into the message's document.
 */
enum class FeedbackStage {
    /** Delivered, not yet turned into a task (the robot runs a few times a day). */
    SENT,
    /** A development task exists and waits in the queue. */
    RECEIVED,
    IN_PROGRESS,
    DONE,
    /** Postponed: in the backlog. */
    LATER,
    /** Closed without a change (not planned or a duplicate). */
    CLOSED,
}

data class FeedbackStatus(
    val stage: FeedbackStage,
    val issueNumber: Int? = null,
    /** When the message became a development task. */
    val issueAt: Instant? = null,
    /** When the robot first saw the task in work (absent if it went straight to done). */
    val inProgressAt: Instant? = null,
    val doneAt: Instant? = null,
    /** The first build (`versionCode`) with the change; `null` — not released yet. */
    val doneBuild: Int? = null,
) {
    /** Done and released: the status won't change, stop asking. */
    val isSettled get() = stage == FeedbackStage.CLOSED || (stage == FeedbackStage.DONE && doneBuild != null)
}

/** One step of the timeline on the message card. [reached] — passed (bright dot) or still ahead (grey). */
data class FeedbackStep(val kind: Kind, val at: Instant?, val reached: Boolean, val build: Int? = null) {
    enum class Kind { SENT, TASK, IN_PROGRESS, DONE, LATER, CLOSED }
}

/** «Что происходит»: sent → became a task → in work → done in build N (or postponed / closed). */
fun feedbackSteps(sentAt: Instant, status: FeedbackStatus?): List<FeedbackStep> {
    val stage = status?.stage ?: FeedbackStage.SENT
    val hasTask = stage != FeedbackStage.SENT
    return buildList {
        add(FeedbackStep(FeedbackStep.Kind.SENT, sentAt, reached = true))
        add(FeedbackStep(FeedbackStep.Kind.TASK, status?.issueAt, reached = hasTask))
        when (stage) {
            FeedbackStage.LATER -> add(FeedbackStep(FeedbackStep.Kind.LATER, null, reached = true))
            FeedbackStage.CLOSED -> add(FeedbackStep(FeedbackStep.Kind.CLOSED, status?.doneAt, reached = true))
            else -> {
                val working = stage == FeedbackStage.IN_PROGRESS || stage == FeedbackStage.DONE
                if (stage != FeedbackStage.DONE || status?.inProgressAt != null) {
                    add(FeedbackStep(FeedbackStep.Kind.IN_PROGRESS, status?.inProgressAt, reached = working))
                }
                add(FeedbackStep(FeedbackStep.Kind.DONE, status?.doneAt, reached = stage == FeedbackStage.DONE, build = status?.doneBuild))
            }
        }
    }
}
