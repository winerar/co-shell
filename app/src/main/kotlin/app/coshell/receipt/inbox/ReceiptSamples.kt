package app.coshell.receipt.inbox

/**
 * The last shared receipt the app could not read (#561), handed once to «Формат чека» feedback as its attachment.
 * `ReceiptLinks` keeps it; previews and tests have none.
 */
fun interface ReceiptSamples {
    /** The file, once; `null` — none waiting. */
    fun take(): SharedFile?

    companion object {
        val NONE = ReceiptSamples { null }
    }
}
