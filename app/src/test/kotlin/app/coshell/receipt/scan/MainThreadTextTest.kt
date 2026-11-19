package app.coshell.receipt.scan

import java.util.concurrent.Executor
import org.junit.Assert.assertEquals
import org.junit.Test

class MainThreadTextTest {
    private class QueueExecutor : Executor {
        val queue = ArrayDeque<Runnable>()
        override fun execute(command: Runnable) {
            queue.addLast(command)
        }
        fun runAll() {
            while (queue.isNotEmpty()) queue.removeFirst().run()
        }
    }

    @Test
    fun `delivers the text only through the main executor`() {
        val main = QueueExecutor()
        val got = mutableListOf<String>()
        val handoff = MainThreadText(main) { got += it }

        handoff("qr")
        assertEquals(emptyList<String>(), got)

        main.runAll()
        assertEquals(listOf("qr"), got)
    }

    @Test
    fun `drops texts after close, also those already queued`() {
        val main = QueueExecutor()
        val got = mutableListOf<String>()
        val handoff = MainThreadText(main) { got += it }

        handoff("queued")
        handoff.close()
        handoff("late")
        main.runAll()

        assertEquals(emptyList<String>(), got)
        assertEquals(0, main.queue.size)
    }
}
