package app.coshell.notices

import android.content.Intent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.YearMonth

/** The screen a notification opens (#745). */
enum class NoticeTarget { NEW_ENTRY, BUDGETS, RECAP, BACKUP, DRAFTS }

/** A tap on a notification: the [target] screen; a limit also names its envelope ([categoryId], [month] like `2026-11`). */
data class NoticeLink(val target: NoticeTarget, val categoryId: String? = null, val month: YearMonth? = null)

/**
 * Hands a tap on a notification to the navigation, like `PaymentLinks`: `MainActivity` offers its intent, `CoShellNavHost`
 * opens the screen and says [consumed]. Opened once: the extra is taken out of the intent.
 */
@Singleton
class NoticeLinks @Inject constructor() {
    private val pending = MutableStateFlow<NoticeLink?>(null)
    val link: StateFlow<NoticeLink?> = pending.asStateFlow()

    /** `true` — the intent came from a notification of ours. */
    fun offer(intent: Intent?): Boolean {
        val target = intent?.getStringExtra(EXTRA)?.let { name -> NoticeTarget.entries.firstOrNull { it.name == name } } ?: return false
        val month = intent.getStringExtra(EXTRA_MONTH)?.let { runCatching { YearMonth.parse(it) }.getOrNull() }
        pending.value = NoticeLink(target, intent.getStringExtra(EXTRA_CATEGORY), month)
        intent.removeExtra(EXTRA)
        intent.removeExtra(EXTRA_CATEGORY)
        intent.removeExtra(EXTRA_MONTH)
        return true
    }

    fun consumed() {
        pending.value = null
    }

    companion object {
        const val EXTRA = "notice_target"
        const val EXTRA_CATEGORY = "notice_category_id"
        const val EXTRA_MONTH = "notice_month"
    }
}
