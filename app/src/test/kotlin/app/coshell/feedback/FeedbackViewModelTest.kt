package app.coshell.feedback

import app.coshell.core.domain.feedback.DeviceInfo
import app.coshell.core.domain.feedback.DeviceInfoProvider
import app.coshell.core.domain.feedback.FeedbackReport
import app.coshell.core.domain.feedback.FeedbackResult
import app.coshell.core.domain.feedback.FeedbackSender
import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus
import app.coshell.core.domain.feedback.FeedbackStatusSource
import app.coshell.core.domain.feedback.SendFeedbackUseCase
import app.coshell.feedback.data.InMemoryFeedbackHistory
import app.coshell.feedback.data.SentFeedback
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
        assertEquals(SentFeedback(clock.now().toEpochMilliseconds(), "Идея: Итог дня", "doc-1", FeedbackStatus(FeedbackStage.SENT)), vm.state.value.history.single())
        assertTrue(vm.state.value.justSent)
        vm.onWriteMore()
        assertFalse(vm.state.value.justSent)
    }

    @Test
    fun `prefill fills an empty field only`() {
        vm.prefill("Синхронизация: код PERMISSION_DENIED.")
        assertEquals("Синхронизация: код PERMISSION_DENIED.", vm.state.value.text)
        vm.onTextChange("Своё")
        vm.prefill("другое")
        assertEquals("Своё", vm.state.value.text)
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
        assertEquals(FeedbackUiState(text = "", result = FeedbackResult.Sent("doc-1")), vm.state.value.copy(history = emptyList()))
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

class FeedbackStatusRefreshTest {
    @get:Rule val main = MainDispatcherRule()

    private val sendFeedback = SendFeedbackUseCase(FakeSender(), object : DeviceInfoProvider {
        override suspend fun deviceInfo() = DeviceInfo("0.1.0", 27, "Pixel", "Android 15", "id")
    }, Clock.System)
    private val done = FeedbackStatus(FeedbackStage.DONE, 105, doneBuild = 248)

    @Test
    fun `opening asks only for messages that may still change and keeps the last status on failure`() {
        val history = InMemoryFeedbackHistory(
            listOf(
                SentFeedback(3, "Ошибка: a", "doc-a", FeedbackStatus(FeedbackStage.SENT)),
                SentFeedback(2, "Идея: b", "doc-b", done),
                SentFeedback(1, "старый", ""),
                SentFeedback(0, "Идея: c", "doc-c", FeedbackStatus(FeedbackStage.RECEIVED, 7)),
            ),
        )
        val asked = mutableListOf<String>()
        val source = object : FeedbackStatusSource {
            override suspend fun status(documentId: String): FeedbackStatus? {
                asked += documentId
                return if (documentId == "doc-a") FeedbackStatus(FeedbackStage.IN_PROGRESS, 160) else null
            }
        }
        val vm = FeedbackViewModel(sendFeedback, history, Clock.System, source)
        assertEquals(listOf("doc-a", "doc-c"), asked)
        val stages = vm.state.value.history.map { it.status?.stage }
        assertEquals(listOf(FeedbackStage.IN_PROGRESS, FeedbackStage.DONE, null, FeedbackStage.RECEIVED), stages)
    }

    @Test
    fun `remove takes the message off the list`() {
        val history = InMemoryFeedbackHistory(listOf(SentFeedback(2, "a", "doc-a", done), SentFeedback(1, "b", "doc-b", done)))
        val vm = FeedbackViewModel(sendFeedback, history)
        vm.remove(2)
        assertEquals(listOf(1L), vm.state.value.history.map { it.sentAtMillis })
    }
}

private class FakeSender : FeedbackSender {
    override val isConfigured = true
    var fail = false
    val sent = mutableListOf<FeedbackReport>()

    override suspend fun send(report: FeedbackReport): String {
        if (fail) error("offline")
        sent += report
        return "doc-${sent.size}"
    }
}
