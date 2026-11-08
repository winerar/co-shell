package app.coshell.core.domain.feedback

import app.coshell.core.domain.update.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class SendFeedbackUseCaseTest {
    private val now = Instant.parse("2026-11-08T10:00:00Z")
    private val device = DeviceInfo("0.1.0", 27, "Google Pixel 8", "Android 15 (API 35)", "install-1")
    private val sender = FakeFeedbackSender()
    private val send = SendFeedbackUseCase(sender, FakeDeviceInfoProvider(device), FixedClock(now))

    @Test
    fun `sends trimmed text with device info and time`() = runTest {
        assertEquals(FeedbackResult.Sent, send("  Не сохраняется заметка \n"))
        assertEquals(listOf(FeedbackReport("Не сохраняется заметка", now, device)), sender.sent)
    }

    @Test
    fun `refuses blank text`() = runTest {
        assertEquals(FeedbackResult.Empty, send(" \n "))
        assertTrue(sender.sent.isEmpty())
    }

    @Test
    fun `refuses text over the limit`() = runTest {
        val max = SendFeedbackUseCase.MAX_LENGTH
        assertEquals(FeedbackResult.TooLong(max), send("a".repeat(max + 1)))
        assertEquals(FeedbackResult.Sent, send("a".repeat(max)))
    }

    @Test
    fun `reports a build without destination`() = runTest {
        sender.isConfigured = false
        assertEquals(FeedbackResult.NotConfigured, send("text"))
        assertTrue(sender.sent.isEmpty())
    }

    @Test
    fun `reports a failed send`() = runTest {
        sender.error = IllegalStateException("HTTP 403")
        assertEquals(FeedbackResult.Failed("HTTP 403"), send("text"))
    }
}

private class FakeFeedbackSender : FeedbackSender {
    override var isConfigured = true
    var error: Exception? = null
    val sent = mutableListOf<FeedbackReport>()

    override suspend fun send(report: FeedbackReport) {
        error?.let { throw it }
        sent += report
    }
}

private class FakeDeviceInfoProvider(private val info: DeviceInfo) : DeviceInfoProvider {
    override suspend fun deviceInfo() = info
}
