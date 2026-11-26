package app.coshell.notices

import android.content.Intent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The screen a notification opens (#745). */
enum class NoticeTarget { NEW_ENTRY, BUDGETS, RECAP, BACKUP, DRAFTS }

/**
 * Hands a tap on a notification to the navigation, like `PaymentLinks`: `MainActivity` offers its intent, `CoShellNavHost`
 * opens the screen and says [consumed]. Opened once: the extra is taken out of the intent.
 */
@Singleton
class NoticeLinks @Inject constructor() {
    private val pending = MutableStateFlow<NoticeTarget?>(null)
    val target: StateFlow<NoticeTarget?> = pending.asStateFlow()

    /** `true` — the intent came from a notification of ours. */
    fun offer(intent: Intent?): Boolean {
        val target = intent?.getStringExtra(EXTRA)?.let { name -> NoticeTarget.entries.firstOrNull { it.name == name } } ?: return false
        pending.value = target
        intent.removeExtra(EXTRA)
        return true
    }

    fun consumed() {
        pending.value = null
    }

    companion object {
        const val EXTRA = "notice_target"
    }
}
