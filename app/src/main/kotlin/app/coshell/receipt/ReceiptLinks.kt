package app.coshell.receipt

import android.content.Intent
import android.net.Uri
import android.os.Build
import app.coshell.core.domain.receipts.ReceiptFormats
import app.coshell.receipt.inbox.Incoming
import app.coshell.receipt.inbox.ReceiptFileReader
import app.coshell.receipt.inbox.ReceiptSamples
import app.coshell.receipt.inbox.SharedFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Hands a receipt from outside to the navigation: a QR link opened by a scanner or the camera (`afip.gob.ar/fe/qr`, #526),
 * or text or a file shared with «Поделиться» (a PDF from Telegram, a photo of a QR). `MainActivity` offers its intent,
 * `CoShellNavHost` opens the entry sheet with [link] and says [consumed]. A share nothing could read opens the blank sheet
 * with «Не удалось прочитать чек» (#561); its file (or text, as `.txt`) waits in [take] for «Формат чека» feedback.
 * Everything is read in the app, offline; links are never opened in a browser.
 */
@Singleton
class ReceiptLinks @Inject constructor(private val files: ReceiptFileReader) : ReceiptSamples {
    /** What to open: [text] for the formats (may be empty), [readable] — a known format reads it. */
    data class Pending(val text: String, val readable: Boolean)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val pending = MutableStateFlow<Pending?>(null)
    val link: StateFlow<Pending?> = pending.asStateFlow()

    /** The last unread share, for «Формат чека» feedback. */
    private var sample: SharedFile? = null

    override fun take(): SharedFile? = sample.also { sample = null }

    /** `true` — the intent carried a receipt, shared text or a shared file. */
    fun offer(intent: Intent?): Boolean {
        intent ?: return false
        val incoming = Incoming.of(
            action = intent.action,
            type = intent.type,
            dataString = intent.dataString,
            text = intent.getStringExtra(Intent.EXTRA_TEXT),
            stream = stream(intent)?.toString(),
        ) ?: return false
        // Taken once: a recreated activity sees the same intent again.
        intent.data = null
        intent.action = Intent.ACTION_MAIN
        scope.launch {
            val (text, file) = when (incoming) {
                // A view link is taken only when a format reads it, so it never needs a sample file.
                is Incoming.Text -> incoming.text to null
                is Incoming.File -> files.read(incoming.uri, incoming.mime).let { it.text to it.file }
            }
            val readable = withContext(Dispatchers.Default) { ReceiptFormats.read(text) != null }
            sample = when {
                readable -> null
                file != null -> file
                text.isNotBlank() -> files.keepText(text)
                else -> null
            }
            pending.value = Pending(text, readable)
        }
        return true
    }

    fun consumed() {
        pending.value = null
    }

    private fun stream(intent: Intent): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } ?: intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
}
