package app.coshell.core.domain.notices

import app.coshell.core.domain.settings.ClockMinutes
import kotlinx.coroutines.flow.Flow

/**
 * Which optional notifications are on and when the daily check runs (#745, board `Not745SetA`). Phone-only, like `ReminderSettings`:
 * each phone of the family decides for itself. Everything is off until the person says otherwise (poll 2026-11-25, «А»).
 */
interface NoticeSettings {
    /** The kinds that are on; empty by default. */
    val enabled: Flow<Set<NoticeKind>>

    suspend fun setEnabled(kind: NoticeKind, on: Boolean)

    /** The time of the daily check on the phone's clock, minutes since midnight; [DEFAULT_TIME] by default. One for all kinds. */
    val time: Flow<Int>

    suspend fun setTime(minutes: Int)

    /** The card «Напоминать вечером о тратах?» on «Обзоре» was answered («Включить» or «Не сейчас»): it never comes back by itself. */
    val offerAnswered: Flow<Boolean>

    suspend fun setOfferAnswered()

    companion object {
        val DEFAULT_TIME = ClockMinutes.of(21)

        /** The times the settings offer as buttons; any other — «Другое…» (#777). */
        val TIMES = listOf(19, 20, 21, 22).map { ClockMinutes.of(it) }
    }
}
