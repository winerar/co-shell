package app.coshell.feedback

import app.coshell.core.domain.feedback.DeviceInfo
import app.coshell.core.domain.feedback.DeviceInfoProvider
import app.coshell.core.domain.feedback.FeedbackReport
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.core.domain.feedback.FeedbackSender
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.feedback.data.InMemoryFeedbackHistory
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Clock
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FeedbackViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val sender = FakeSender()
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T10:00:00Z")
    }
    private val device = object : DeviceInfoProvider {
        override suspend fun deviceInfo() = DeviceInfo("0.1.0", 27, "Google Pixel 8", "Android 15 (API 35)", "id")
    }
    // Lazy: the ViewModel starts collecting the history in init, after MainDispatcherRule is in place.
    private val vm by lazy { FeedbackViewModel(SendFeedbackUseCase(sender, device, clock)) }

    @Test
    fun `kind goes in front of the text and the sent message is remembered`() {
        val history = InMemoryFeedbackHistory()
        val vm = FeedbackViewModel(SendFeedbackUseCase(sender, device, clock), history, clock)
        vm.onKind(FeedbackKind.IDEA)
        vm.onTextChange(" Итог дня ")
        vm.send("Идея")
        assertEquals(listOf("Идея: Итог дня"), sender.sent.map { it.text })
        assertEquals(listOf("Идея: Итог дня"), vm.state.value.history.map { it.text })
        assertTrue(vm.state.value.justSent)
        vm.onWriteMore()
        assertFalse(vm.state.value.justSent)
    }

    @Test
    fun `send is disabled for blank text`() {
        vm.onTextChange("  ")
        assertFalse(vm.state.value.canSend)
        vm.send()
        assertTrue(sender.sent.isEmpty())
    }

    @Test
    fun `successful send clears the field`() {
        vm.onTextChange("Нет итога за день")
        vm.send()

        assertEquals(listOf("Нет итога за день"), sender.sent.map { it.text })
        assertEquals(FeedbackUiState(text = "", result = FeedbackResult.Sent), vm.state.value.copy(history = emptyList()))
    }

    @Test
    fun `failed send keeps the text and typing hides the result`() {
        sender.fail = true
        vm.onTextChange("Ошибка")
        vm.send()

        assertEquals("Ошибка", vm.state.value.text)
        assertEquals(FeedbackResult.Failed("offline"), vm.state.value.result)

        vm.onTextChange("Ошибка!")
        assertNull(vm.state.value.result)
    }
}

private class FakeSender : FeedbackSender {
    override val isConfigured = true
    var fail = false
    val sent = mutableListOf<FeedbackReport>()

    override suspend fun send(report: FeedbackReport) {
        if (fail) error("offline")
        sent += report
    }
}
