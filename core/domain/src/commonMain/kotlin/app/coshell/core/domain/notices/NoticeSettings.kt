package app.coshell.core.domain.notices

import kotlinx.coroutines.flow.Flow

/**
 * Which optional notifications are on and when the daily check runs (#745, board `Not745SetA`). Phone-only, like `ReminderSettings`:
 * each phone of the family decides for itself. Everything is off until the person says otherwise (poll 2026-11-25, «А»).
 */
interface NoticeSettings {
    /** The kinds that are on; empty by default. */
    val enabled: Flow<Set<NoticeKind>>

    suspend fun setEnabled(kind: NoticeKind, on: Boolean)

    /** The hour of the daily check on the phone's clock; [DEFAULT_HOUR] by default. One for all kinds. */
    val hour: Flow<Int>

    suspend fun setHour(hour: Int)

    /** The card «Напоминать вечером о тратах?» on «Обзоре» was answered («Включить» or «Не сейчас»): it never comes back by itself. */
    val offerAnswered: Flow<Boolean>

    suspend fun setOfferAnswered()

    companion object {
        const val DEFAULT_HOUR = 21

        /** The hours the settings offer. */
        val HOURS = listOf(19, 20, 21, 22)
    }
}
