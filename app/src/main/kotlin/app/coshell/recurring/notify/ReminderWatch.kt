package app.coshell.recurring.notify

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.recurring.activePayments
import app.coshell.core.domain.recurring.stillToRemind
import app.coshell.core.domain.templates.TemplateRepository
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Takes the 9:00 reminder back once its payments are handled (#406, board `Rec143Edge`, «на двоих»): posted or
 * skipped in the app, or on the other phone — its `handledThrough` arrives by sync and lands in Room, which this
 * watches. Several payments and only some handled — the reminder is redrawn with the rest, quietly. A paused
 * payment (account archived since) drops out too. Lives as long as the app's process (sync runs in it as well).
 */
class ReminderWatch @Inject constructor(
    private val templates: TemplateRepository,
    private val accounts: AccountRepository,
    private val notices: PaymentNotices,
    private val log: ReminderLog,
    private val clock: Clock,
    private val zone: TimeZone,
) {
    suspend fun run() {
        combine(templates.observeTemplates(), accounts.observeAccounts(includeArchived = true)) { list, accountList ->
            val today = clock.todayIn(zone)
            val reminded = log.remindedFor(today)
            if (reminded.isEmpty()) return@combine
            if (!notices.reminderShown()) {
                // Swiped away or put off with «Завтра»: nothing on screen to take back.
                log.updateReminded(emptySet())
                return@combine
            }
            val left = stillToRemind(reminded, activePayments(list, accountList.associateBy { it.id }), today)
            if (left.size == reminded.size) return@combine
            if (left.isEmpty()) notices.cancelReminder() else notices.remind(left, today)
            log.updateReminded(left.map { it.id })
        }.collect()
    }
}
