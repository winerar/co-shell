package app.coshell.shortcuts

import android.content.Context
import android.content.Intent
import app.coshell.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Hands a launcher shortcut (#555) to the navigation, like `PaymentLinks`: `MainActivity` offers its intent,
 * `CoShellNavHost` opens the entry sheet and says [consumed]. The sheet is the one «+» opens, so a locked input
 * (#334) and the PIN screen behave the same.
 */
@Singleton
class ShortcutLinks @Inject constructor() {
    private val pending = MutableStateFlow<ShortcutLink?>(null)
    val link: StateFlow<ShortcutLink?> = pending.asStateFlow()

    /** `true` — the intent came from a shortcut. */
    fun offer(intent: Intent?): Boolean {
        val link = ShortcutLink.of(intent?.getStringExtra(ShortcutLink.EXTRA_TYPE), intent?.getStringExtra(ShortcutLink.EXTRA_TEMPLATE)) ?: return false
        pending.value = link
        // Opened once: a recreated activity sees the same intent again.
        intent?.removeExtra(ShortcutLink.EXTRA_TYPE)
        intent?.removeExtra(ShortcutLink.EXTRA_TEMPLATE)
        return true
    }

    fun consumed() {
        pending.value = null
    }

    companion object {
        const val ACTION = "app.coshell.action.SHORTCUT"

        /** The intent of a shortcut: a new operation of [type], or the sheet filled from the template [templateId]. */
        fun intent(context: Context, type: String? = null, templateId: String? = null): Intent =
            Intent(context, MainActivity::class.java).setAction(ACTION)
                .putExtra(ShortcutLink.EXTRA_TYPE, type).putExtra(ShortcutLink.EXTRA_TEMPLATE, templateId)
    }
}
