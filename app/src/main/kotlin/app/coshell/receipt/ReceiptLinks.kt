package app.coshell.receipt

import android.content.Intent
import app.coshell.core.domain.quickentry.AfipReceiptQr
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hands the QR link of a receipt from outside (a scanner or the camera opening `afip.gob.ar/fe/qr`, #526) to the
 * navigation: `MainActivity` offers its intent, `CoShellNavHost` opens the entry sheet and says [consumed].
 * The link is read in the app, offline — it is never opened in a browser.
 */
@Singleton
class ReceiptLinks @Inject constructor() {
    private val pending = MutableStateFlow<String?>(null)
    val link: StateFlow<String?> = pending.asStateFlow()

    /** `true` — the intent carried a receipt link. */
    fun offer(intent: Intent?): Boolean {
        val text = textOf(intent?.action, intent?.dataString) ?: return false
        pending.value = text
        // Opened once: a recreated activity sees the same intent again.
        intent?.data = null
        return true
    }

    fun consumed() {
        pending.value = null
    }

    companion object {
        /** The text with the receipt link of a «view link» intent, or `null` when it is something else. */
        fun textOf(action: String?, dataString: String?): String? =
            dataString?.takeIf { action == Intent.ACTION_VIEW && AfipReceiptQr.find(it) != null }
    }
}
