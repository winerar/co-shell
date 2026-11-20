package app.coshell.receipt.inbox

import android.content.Intent
import app.coshell.core.domain.receipts.ReceiptFormats

/**
 * What came into the app that may be a receipt: a link opened from a scanner (#526), text or a file shared with
 * «Поделиться» (a Telegram PDF, a photo of a QR, a bank message). Decided from intent fields only, so it is testable.
 */
sealed interface Incoming {
    data class Text(val text: String) : Incoming

    /** A shared file: read later by [ReceiptFileReader] (PDF text layer and QR, image QR). */
    data class File(val uri: String, val mime: String?) : Incoming

    companion object {
        /**
         * [action]/[type] of the intent, [dataString] — its data, [text] — `EXTRA_TEXT`, [stream] — `EXTRA_STREAM` as text.
         * A view link counts only when a known format reads it (other links are not ours); shared text always counts:
         * the entry sheet opens either way, filled when the text is a receipt.
         */
        fun of(action: String?, type: String?, dataString: String?, text: String?, stream: String?): Incoming? = when (action) {
            Intent.ACTION_VIEW -> dataString?.takeIf { ReceiptFormats.read(it) != null }?.let(::Text)
            Intent.ACTION_SEND -> when {
                stream != null && (type == null || isFile(type)) -> File(stream, type)
                !text.isNullOrBlank() -> Text(text.take(ReceiptFormats.MAX_TEXT))
                else -> null
            }
            else -> null
        }

        private fun isFile(type: String) = type == PDF || type.startsWith("image/") || type.startsWith("text/")

        const val PDF = "application/pdf"
    }
}
