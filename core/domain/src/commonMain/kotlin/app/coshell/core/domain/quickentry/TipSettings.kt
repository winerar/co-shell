package app.coshell.core.domain.quickentry

/**
 * What «Чаевые» remembers between operations (#322, owner 2026-11-17 А): the category tips are written to — asked
 * once, or «как у покупки» ([TipPrefs.SAME_AS_PURCHASE]) — and the account they were paid from last time. `null` — not chosen yet (or gone: the caller checks).
 */
data class TipPrefs(val categoryId: String? = null, val accountId: String? = null) {
    companion object {
        /** «Как у покупки» chosen as the tip category: the tip takes the category of each purchase. */
        const val SAME_AS_PURCHASE = "@purchase"
    }
}

/** Device-only storage of [TipPrefs]; the Android implementation keeps it in SharedPreferences. */
interface TipSettings {
    suspend fun load(): TipPrefs
    suspend fun save(prefs: TipPrefs)
}
