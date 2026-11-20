package app.coshell.core.domain.feedback

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

/** Outcome of [SendFeedbackUseCase]. */
sealed interface FeedbackResult {
    /** [documentId] — where the message is stored, to read its status later (empty if unknown). */
    data class Sent(val documentId: String = "") : FeedbackResult
    data object Empty : FeedbackResult
    data class TooLong(val maxLength: Int) : FeedbackResult
    data class TooManyFiles(val maxCount: Int) : FeedbackResult
    data class FilesTooLarge(val maxBytes: Int) : FeedbackResult
    /** This build has nowhere to send feedback (local builds). */
    data object NotConfigured : FeedbackResult
    data class Failed(val reason: String) : FeedbackResult
}

/** Trims the text, checks its length and the files, adds [DeviceInfo] and technical lines and sends it. */
class SendFeedbackUseCase(
    private val sender: FeedbackSender,
    private val deviceInfo: DeviceInfoProvider,
    private val clock: Clock,
    private val diagnostics: DiagnosticsProvider = DiagnosticsProvider { emptyList() },
) {
    suspend operator fun invoke(text: String, attachments: List<FeedbackAttachment> = emptyList()): FeedbackResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return FeedbackResult.Empty
        if (trimmed.length > MAX_LENGTH) return FeedbackResult.TooLong(MAX_LENGTH)
        FeedbackAttachment.problem(attachments)?.let { return it }
        if (!sender.isConfigured) return FeedbackResult.NotConfigured
        return try {
            val report = FeedbackReport(trimmed, clock.now(), deviceInfo.deviceInfo(), technicalLines(), attachments)
            FeedbackResult.Sent(sender.send(report))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FeedbackResult.Failed(e.message ?: e::class.simpleName.orEmpty())
        }
    }

    /** Missing technical data must never stop a message. */
    private suspend fun technicalLines(): List<String> = try {
        diagnostics.diagnostics()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        listOf("diagnostics failed: ${e::class.simpleName}")
    }

    companion object {
        /** Must not exceed the limit in `firebase/firestore.rules` (4000 there, so older builds still work). */
        const val MAX_LENGTH = 500
    }
}
