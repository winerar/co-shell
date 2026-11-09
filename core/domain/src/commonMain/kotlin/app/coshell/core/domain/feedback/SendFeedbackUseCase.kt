package app.coshell.core.domain.feedback

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

/** Outcome of [SendFeedbackUseCase]. */
sealed interface FeedbackResult {
    data object Sent : FeedbackResult
    data object Empty : FeedbackResult
    data class TooLong(val maxLength: Int) : FeedbackResult
    /** This build has nowhere to send feedback (local builds). */
    data object NotConfigured : FeedbackResult
    data class Failed(val reason: String) : FeedbackResult
}

/** Trims the text, checks its length, adds [DeviceInfo] and sends it. */
class SendFeedbackUseCase(
    private val sender: FeedbackSender,
    private val deviceInfo: DeviceInfoProvider,
    private val clock: Clock,
) {
    suspend operator fun invoke(text: String): FeedbackResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return FeedbackResult.Empty
        if (trimmed.length > MAX_LENGTH) return FeedbackResult.TooLong(MAX_LENGTH)
        if (!sender.isConfigured) return FeedbackResult.NotConfigured
        return try {
            sender.send(FeedbackReport(trimmed, clock.now(), deviceInfo.deviceInfo()))
            FeedbackResult.Sent
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FeedbackResult.Failed(e.message ?: e::class.simpleName.orEmpty())
        }
    }

    companion object {
        /** Must not exceed the limit in `firebase/firestore.rules` (4000 there, so older builds still work). */
        const val MAX_LENGTH = 500
    }
}
