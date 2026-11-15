package app.coshell.core.domain.quickentry

import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * What was typed in the entry sheet when it closed without saving (#323). One draft, on this phone only (not synced):
 * the next «+» offers «Продолжить» with it.
 */
data class EntryDraft(
    val type: TransactionType,
    val accountId: String?,
    val amount: Money?,
    val categoryId: String?,
    val note: String,
    val tagIds: List<String>,
    val savedAt: Instant,
)

object EntryDrafts {
    /** After half a day the purchase is forgotten anyway — an old draft would only confuse. */
    val MAX_AGE: Duration = 12.hours

    /** Keep a draft only when something was really typed: an amount or a note (a picked category alone is a tap away). */
    fun worthKeeping(amount: Money?, note: String): Boolean = amount?.isPositive == true || note.isNotBlank()

    /** The draft to offer at [now], or `null` when there is none or it is too old (a clock moved back counts as too old). */
    fun toOffer(draft: EntryDraft?, now: Instant): EntryDraft? {
        if (draft == null) return null
        val age = now - draft.savedAt
        return draft.takeIf { !age.isNegative() && age <= MAX_AGE }
    }
}
