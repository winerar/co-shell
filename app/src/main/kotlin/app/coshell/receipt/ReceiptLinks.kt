package app.coshell.receipt

import android.content.Intent
import android.net.Uri
import android.os.Build
import app.coshell.receipt.inbox.Incoming
import app.coshell.receipt.inbox.ReceiptFileReader
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

/**
 * Hands a receipt from outside to the navigation: a QR link opened by a scanner or the camera (`afip.gob.ar/fe/qr`, #526),
 * or text or a file shared with «Поделиться» (a PDF from Telegram, a photo of a QR). `MainActivity` offers its intent,
 * `CoShellNavHost` opens the entry sheet with [link] — the text the formats read (empty: nothing recognised, the sheet
 * opens blank) — and says [consumed]. Everything is read in the app, offline; links are never opened in a browser.
 */
@Singleton
class ReceiptLinks @Inject constructor(private val files: ReceiptFileReader) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val pending = MutableStateFlow<String?>(null)
    val link: StateFlow<String?> = pending.asStateFlow()

    /** The last shared file, kept for «Формат чека» feedback when the receipt was not recognised. */
    private val lastFile = MutableStateFlow<SharedFile?>(null)
    val sharedFile: StateFlow<SharedFile?> = lastFile.asStateFlow()

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
        when (incoming) {
            is Incoming.Text -> pending.value = incoming.text
            is Incoming.File -> scope.launch {
                val read = files.read(incoming.uri, incoming.mime)
                lastFile.value = read.file
                pending.value = read.text
            }
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
