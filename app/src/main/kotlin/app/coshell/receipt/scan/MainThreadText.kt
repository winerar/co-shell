package app.coshell.receipt.scan

import java.util.concurrent.Executor

/**
 * Hands a QR text read on the camera analysis thread to [onText] on [main] (#531: navigating from the analysis
 * thread crashed with «setCurrentState must be called on the main thread»). After [close] nothing is delivered —
 * a frame that finishes after the scanner left must not open the entry sheet.
 */
internal class MainThreadText(private val main: Executor, private val onText: (String) -> Unit) : (String) -> Unit {
    @Volatile private var closed = false

    override fun invoke(text: String) {
        if (closed) return
        main.execute { if (!closed) onText(text) }
    }

    fun close() {
        closed = true
    }
}
