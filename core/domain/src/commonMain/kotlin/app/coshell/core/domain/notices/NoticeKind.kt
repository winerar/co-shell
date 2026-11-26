package app.coshell.core.domain.notices

/**
 * The optional local notifications (#745, `hints.md` §«Уведомления»): none of them goes out until the person turns it on.
 * [key] is stored on the phone — never rename. The order of the entries is the order they win a day: at most one a day.
 * Payment reminders and «Трата партнёра» are older switches and live apart (`ReminderSettings`, `PartnerSpend`).
 */
enum class NoticeKind(val key: String) {
    /** A limit that is 80–99 % spent (`LimitWatch`): «ещё можно…», a reference point and not a ban. */
    LIMIT("limit"),

    /** The first days of a new month: the recap of the last one is ready. */
    RECAP("recap"),

    /** Three days or more without an entry: once per silence, no reproach. */
    COMEBACK("comeback"),

    /** Two weeks without a saved copy. */
    BACKUP("backup"),

    /** Three or more ready drafts of quick notes wait to be posted. */
    DRAFTS("drafts"),

    /** In the evening, if nothing was written today. */
    EVENING("evening"),
    ;

    companion object {
        fun fromKey(key: String): NoticeKind? = entries.firstOrNull { it.key == key }
    }
}
