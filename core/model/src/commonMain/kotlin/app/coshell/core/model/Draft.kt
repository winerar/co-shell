package app.coshell.core.model

/** Where a draft came from. Only notes now; bank notifications and statements join later (#592, #331). */
enum class DraftOrigin { NOTE }

/** How the category of a draft was found — the grey caption under it («по прошлым операциям», «по названию», «предложено ИИ»). */
enum class DraftCategorySource { HISTORY, DICTIONARY, AI }

/**
 * An unfinished expense (#586): a line of a quick note read by rules or the on-device model, waiting for the person to check it.
 * Lives only on this phone (no sync, no backup). Anything may be missing: [amountMinor] (the line had no number), [accountId], [categoryId].
 * [currency] is the currency of the amount; it is posted as it is, so the account must have the same one.
 */
data class Draft(
    val id: String,
    val title: String,
    val currency: CurrencyCode,
    val amountMinor: Long?,
    val accountId: String?,
    val categoryId: String?,
    val categorySource: DraftCategorySource?,
    /** The day of the expense, epoch ms UTC: the day the note was processed, or earlier for «вчера». */
    val dateMillis: Long,
    val createdAt: Long,
    val origin: DraftOrigin = DraftOrigin.NOTE,
) {
    val money: Money? get() = amountMinor?.let { Money(it, currency) }
}
