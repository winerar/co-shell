package app.coshell.receipt.inbox

/**
 * The text of the last shared receipt no format could read (#593): «Прочитать с ИИ» in the entry sheet gives it to the model.
 * Kept until the next share (the window may be opened again after «Отменить»); never saved.
 */
fun interface UnreadReceipts {
    /** The text, `null` — nothing unread waiting (or a photo without a text layer). */
    fun text(): String?

    companion object {
        val NONE = UnreadReceipts { null }
    }
}
